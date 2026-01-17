package twitch

import (
	"dawpitech/area/models"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/twitch"
	"os"
)

var Provider = models.Service{
	Name:   "Twitch",
	Hidden: false,
	Actions: []models.Action{
		{
			Name:          "twitch_followed_streamer_is_live",
			PrettyName:    "Followed streamer started a live on Twitch",
			Description:   "Trigger when at least one of your followed streamer start a live on Twitch, will not trigger until everybody goes offline.",
			Parameters:    nil,
			Outputs:       nil,
			SetupTrigger:  TriggerFollowedStreamerIsLive,
			RemoveTrigger: RemoveFollowedStreamerIsLive,
		},
	},
	Modifiers: nil,
	Reactions: nil,
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthTwitchInit,
		HandlerAuthCallback: AuthTwitchCallback,
		HandlerAuthCheck:    AuthTwitchCheck,
	},
	DBModels: []interface{}{
		&ProviderTwitchAuthData{},
	},
}

var oauthConfig = &oauth2.Config{
	ClientID:     os.Getenv("TWITCH_OAUTH2_CLIENT_ID"),
	ClientSecret: os.Getenv("TWITCH_OAUTH2_CLIENT_SECRET"),
	Endpoint:     twitch.Endpoint,
	RedirectURL:  os.Getenv("PUBLIC_URL") + "/providers/twitch/auth/callback",
	Scopes: []string{
		"user:read:follows",
	},
}
