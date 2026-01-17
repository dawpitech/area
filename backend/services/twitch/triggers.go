package twitch

import (
	"context"
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"fmt"
	"github.com/adeithe/go-twitch/api"
	"github.com/go-co-op/gocron/v2"
	"github.com/google/uuid"
	"github.com/juju/errors"
	"golang.org/x/oauth2"
	"io"
	"log"
	"net/http"
)

var scheduler gocron.Scheduler
var workflowJobUUID = make(map[uint]uuid.UUID)

type TwitchUser struct {
	ID              string `json:"id"`
	Login           string `json:"login"`
	DisplayName     string `json:"display_name"`
	Type            string `json:"type"`
	BroadcasterType string `json:"broadcaster_type"`
}

type TwitchUsersResponse struct {
	Data []TwitchUser `json:"data"`
}

type TwitchFollow struct {
	FromID     string `json:"from_id"`
	FromLogin  string `json:"from_login"`
	FromName   string `json:"from_name"`
	ToID       string `json:"to_id"`
	ToLogin    string `json:"to_login"`
	ToName     string `json:"to_name"`
	FollowedAt string `json:"followed_at"`
}

type TwitchFollowsResponse struct {
	Data       []TwitchFollow `json:"data"`
	Pagination struct {
		Cursor string `json:"cursor"`
	} `json:"pagination"`
	Total int `json:"total"`
}

type TwitchStream struct {
	ID           string   `json:"id"`
	UserID       string   `json:"user_id"`
	UserLogin    string   `json:"user_login"`
	UserName     string   `json:"user_name"`
	GameID       string   `json:"game_id"`
	GameName     string   `json:"game_name"`
	Type         string   `json:"type"`
	Title        string   `json:"title"`
	ViewerCount  int      `json:"viewer_count"`
	StartedAt    string   `json:"started_at"`
	Language     string   `json:"language"`
	ThumbnailURL string   `json:"thumbnail_url"`
	TagIDs       []string `json:"tag_ids"`
	IsMature     bool     `json:"is_mature"`
}

type TwitchStreamsResponse struct {
	Data       []TwitchStream `json:"data"`
	Pagination struct {
		Cursor string `json:"cursor"`
	} `json:"pagination"`
}

func RemoveFollowedStreamerIsLive(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func makeTwitchAPIRequest(token oauth2.Token, clientID string, url string) ([]byte, error) {
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}

	req.Header.Set("Client-Id", clientID)

	client := oauthConfig.Client(context.Background(), &token)
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err.Error())
		}
	}(resp.Body)

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	return io.ReadAll(resp.Body)
}

func checkFollowedStreamerIsLive(ctx models.Context) {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderTwitchAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Google Account linked, a github action cannot be used.")
		return
	}

	var OwnerOAuth2Access ProviderTwitchAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	token := oauth2.Token{
		AccessToken: OwnerOAuth2Access.AccessToken,
		TokenType:   "Bearer",
	}

	// Get the authenticated user's information
	userBody, err := makeTwitchAPIRequest(
		token,
		oauthConfig.ClientID,
		"https://api.twitch.tv/helix/users",
	)
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to get Twitch user information:  "+err.Error())
		return
	}

	var usersResp TwitchUsersResponse
	if err := json.Unmarshal(userBody, &usersResp); err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse user response: "+err.Error())
		return
	}

	if len(usersResp.Data) == 0 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Twitch user found.")
		return
	}

	userID := usersResp.Data[0].ID

	// Get the list of followed channels
	followsURL := fmt.Sprintf("https://api.twitch.tv/helix/channels/followed?user_id=%s&first=100", userID)
	followsBody, err := makeTwitchAPIRequest(
		OwnerOAuth2Access.AccessToken,
		clientID,
		followsURL,
	)
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to get followed channels: "+err.Error())
		return
	}

	var followsResp TwitchFollowsResponse
	if err := json.Unmarshal(followsBody, &followsResp); err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse follows response: "+err.Error())
		return
	}

	if len(followsResp.Data) == 0 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "No followed channels found.")
		return
	}

	// Collect all followed broadcaster IDs (max 100 per request)
	broadcasterIDs := make([]string, 0, len(followsResp.Data))
	for _, follow := range followsResp.Data {
		broadcasterIDs = append(broadcasterIDs, follow.ToID)
		if len(broadcasterIDs) >= 100 {
			break // Twitch API limit
		}
	}

	// Build the streams URL with user_id parameters
	streamsURL := "https://api.twitch.tv/helix/streams?"
	for i, id := range broadcasterIDs {
		if i > 0 {
			streamsURL += "&"
		}
		streamsURL += fmt.Sprintf("user_id=%s", id)
	}

	// Check which of the followed streamers are currently live
	streamsBody, err := makeTwitchAPIRequest(
		OwnerOAuth2Access.AccessToken,
		clientID,
		streamsURL,
	)
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to get live streams: "+err.Error())
		return
	}

	var streamsResp TwitchStreamsResponse
	if err := json.Unmarshal(streamsBody, &streamsResp); err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse streams response:  "+err.Error())
		return
	}

	// If at least one followed streamer is live, trigger the workflow
	if len(streamsResp.Data) > 0 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "At least one followed streamer is live!")

		// Log details about live streamers
		for _, stream := range streamsResp.Data {
			logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog,
				fmt.Sprintf("Live: %s - %s (Viewers: %d)", stream.UserName, stream.Title, stream.ViewerCount))
		}

		// TODO: Trigger the workflow action here
		// You might want to call a function to execute the workflow's actions
		// or set a flag/event that indicates a followed streamer is live

	} else {
		logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "No followed streamers are currently live.")
	}
}

func TriggerFollowedStreamerIsLive(ctx models.Context) error {
	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkFollowedStreamerIsLive, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}
