package github

import (
	"dawpitech/area/models"
	_ "github.com/joho/godotenv/autoload" // Assure that
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/github"
	"os"
)

var Provider = models.Service{
	Name:      "Github",
	Hidden:    false,
	Actions:   nil,
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
