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
				},
				{
					Name:       "issue_name",
					PrettyName: "Issue name",
				},
				{
					Name:       "issue_content",
					PrettyName: "Issue content",
				},
			},
			Handler: HandlerCreateAnIssue,
		},
	},
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthInit,
		HandlerAuthCallback: AuthCallback,
		HandlerAuthCheck:    AuthCheck,
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
