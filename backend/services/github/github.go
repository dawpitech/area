package github

import (
	"dawpitech/area/models"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/github"
	"os"
)

var oauthConfig = &oauth2.Config{
	ClientID:     os.Getenv("DISCORD_OAUTH2_CLIENT_ID"),
	ClientSecret: os.Getenv("DISCORD_OAUTH2_CLIENT_SECRET"),
	Endpoint:     github.Endpoint,
	RedirectURL:  "http://localhost:3000/auth/github/callback",
	Scopes:       nil,
}

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
	AuthMethod: nil,
	DBModels: []interface{}{
		&ProviderGithubAuthData{},
	},
}
