package github

import (
	"dawpitech/area/models"
	_ "github.com/joho/godotenv/autoload" // Assure that
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/github"
	"os"
)

var Provider = models.Service{
	Name:   "Github",
	Hidden: false,
	Actions: []models.Action{
		{
			Name:        "github_new_star",
			PrettyName:  "Receive a star on a Github repository",
			Description: "Trigger when a new star is added on the specified Github repository",
			Parameters: []models.Parameter{
				{
					Name:       "star_target_repository",
					PrettyName: "Target repository",
					Type:       models.String,
				},
			},
			Outputs: []models.Parameter{
				{
					Name:       "new_star_user",
					PrettyName: "User that had the star",
					Type:       models.String,
				},
			},
			SetupTrigger:  TriggerNewStarOnRepo,
			RemoveTrigger: RemoveNewStarOnRepo,
		},
	},
	Modifiers: nil,
	Reactions: []models.Reaction{
		{
			Name:        "github_create_issue",
			PrettyName:  "Create an issue",
			Description: "Create an issue on a github repository",
			Parameters: []models.Parameter{
				{
					Name:       "target_repository",
					PrettyName: "Target repository",
					Type:       models.String,
				},
				{
					Name:       "issue_name",
					PrettyName: "Issue name",
					Type:       models.String,
				},
				{
					Name:       "issue_content",
					PrettyName: "Issue content",
					Type:       models.String,
				},
			},
			Handler: HandlerCreateAnIssue,
		},
	},
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthGithubInit,
		HandlerAuthCallback: AuthGithubCallback,
		HandlerAuthCheck:    AuthGithubCheck,
	},
	WebhookEndpoints: nil,
	DBModels: []interface{}{
		&ProviderGithubAuthData{},
	},
}

var oauthConfig = &oauth2.Config{
	ClientID:     os.Getenv("GITHUB_OAUTH2_CLIENT_ID"),
	ClientSecret: os.Getenv("GITHUB_OAUTH2_CLIENT_SECRET"),
	Endpoint:     github.Endpoint,
	RedirectURL:  os.Getenv("PUBLIC_URL") + "/providers/github/auth/callback",
	Scopes: []string{
		"repo",
	},
}
