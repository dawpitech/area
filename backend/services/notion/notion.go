package notion

import (
	"dawpitech/area/models"
	"os"

	_ "github.com/joho/godotenv/autoload" // Assure that
	"golang.org/x/oauth2"
)

var Provider = models.Service{
	Name:      "Notion",
	Hidden:    false,
	Actions:   []models.Action{},
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
	WebhookEndpoints: nil,
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
