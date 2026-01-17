package notion

import (
	"dawpitech/area/models"
	"os"

	_ "github.com/joho/godotenv/autoload" // Assure that
	"golang.org/x/oauth2"
)

var Provider = models.Service{
	Name:   "Notion",
	Hidden: false,
	Actions: []models.Action{
		{
			Name:        "notion_page_created",
			PrettyName:  "New notion page created",
			Description: "Trigger when a new notion page is created",
			Parameters:  nil,
			Outputs: []models.Parameter{
				{
					Name:       "page_id",
					PrettyName: "ID of the page that was created",
					Type:       models.String,
				},
				{
					Name:       "timestamp",
					PrettyName: "Timestamp of the event",
					Type:       models.String,
				},
				{
					Name:       "workspace_id",
					PrettyName: "ID of the workspace",
					Type:       models.String,
				},
			},
			SetupTrigger:  SetupNotionPageCreatedTrigger,
			RemoveTrigger: RemoveNotionPageCreatedTrigger,
		},
		{
			Name:        "notion_page_deleted",
			PrettyName:  "Notion page deleted",
			Description: "Trigger when a notion page is deleted",
			Parameters:  nil,
			Outputs: []models.Parameter{
				{
					Name:       "page_id",
					PrettyName: "ID of the page that was deleted",
					Type:       models.String,
				},
				{
					Name:       "timestamp",
					PrettyName: "Timestamp of the event",
					Type:       models.String,
				},
				{
					Name:       "workspace_id",
					PrettyName: "ID of the workspace",
					Type:       models.String,
				},
			},
			SetupTrigger:  SetupNotionPageDeletedTrigger,
			RemoveTrigger: RemoveNotionPageDeletedTrigger,
		},
		{
			Name:        "notion_page_restored",
			PrettyName:  "Notion page restored",
			Description: "Trigger when a notion page is restored (undeleted)",
			Parameters:  nil,
			Outputs: []models.Parameter{
				{
					Name:       "page_id",
					PrettyName: "ID of the page that was restored",
					Type:       models.String,
				},
				{
					Name:       "timestamp",
					PrettyName: "Timestamp of the event",
					Type:       models.String,
				},
				{
					Name:       "workspace_id",
					PrettyName: "ID of the workspace",
					Type:       models.String,
				},
			},
			SetupTrigger:  SetupNotionPageRestoredTrigger,
			RemoveTrigger: RemoveNotionPageRestoredTrigger,
		},
	},
	Modifiers: nil,
	Reactions: []models.Reaction{
		{
			Name:        "notion_respond_to_thread",
			PrettyName:  "Respond to a thread",
			Description: "Respond to a thread on Notion",
			Parameters: []models.Parameter{
				{
					Name:       "discussion_id",
					PrettyName: "Discussion ID",
					Type:       models.String,
				},
				{
					Name:       "comment_content",
					PrettyName: "Comment content",
					Type:       models.String,
				},
			},
			Handler: HandlerNotionRespondToThread,
		},
	},
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthNotionInit,
		HandlerAuthCallback: AuthNotionCallback,
		HandlerAuthCheck:    AuthNotionCheck,
	},
	WebhookEndpoints: []models.WebhookEndpoint{
		{
			EndpointURL:   "/providers/notion/webhooks/1",
			HandlerMethod: TriggerNotion,
		},
	},
	DBModels: []interface{}{
		&ProviderNotionAuthData{},
	},
}

var Endpoint = oauth2.Endpoint{
	AuthURL:       "https://api.notion.com/v1/oauth/authorize",
	TokenURL:      "https://api.notion.com/v1/oauth/token",
	DeviceAuthURL: "https://api.notion.com/v1/oauth/token",
}

var oauthConfig = &oauth2.Config{
	ClientID:     os.Getenv("NOTION_OAUTH2_CLIENT_ID"),
	ClientSecret: os.Getenv("NOTION_OAUTH2_CLIENT_SECRET"),
	Endpoint:     Endpoint,
	RedirectURL:  os.Getenv("PUBLIC_URL") + "/providers/notion/auth/callback",
	Scopes: []string{
		"repo",
	},
}
