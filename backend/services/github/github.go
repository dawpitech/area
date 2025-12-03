package github

import (
	"dawpitech/area/models"
	_ "github.com/joho/godotenv/autoload" // Assure that
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/github"
	"os"
)

var AuthStateMap = map[string]uint{}

var Provider = models.Service{
	Name:    "Github",
	Icon:    "",
	Actions: []models.Action{},
	Reactions: []models.Reaction{
		{
			Name: "Create an issue",
			Parameters: []string{
				"target_repository",
			},
		},
	},
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthInit,
		HandlerAuthCallback: AuthCallback,
		RouteAuthInit:       "/providers/github/auth/init",
		RouteAuthCallback:   "/providers/github/auth/callback",
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
	Scopes:       nil,
}
