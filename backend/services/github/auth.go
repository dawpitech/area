package github

import (
	"context"
	"dawpitech/area/initializers"
	"dawpitech/area/models/routes"
	"dawpitech/area/utils"
	"encoding/hex"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"golang.org/x/oauth2"
	"math/rand"
	"net/http"
	"os"
	"strings"
)

type AuthInitInfo struct {
	UserID   uint
	Platform string
}

var AuthStateMap = map[string]AuthInitInfo{}

func scopeStringFromToken(token *oauth2.Token) (string, bool) {
	if token == nil {
		return "", false
	}
	raw := token.Extra("scope")
	if raw == nil {
		return "", false
	}

	switch v := raw.(type) {
	case string:
		return v, true
	case []string:
		return strings.Join(v, " "), true
	default:
		return "", false
	}
}

func AuthInit(g *gin.Context, in *routes.ThirdPartyAuthInit) error {
	maybeUser, ok := g.Get("user")

	if !ok {
		return errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return errors.BadRequest
	}

	bytes := make([]byte, 16)
	if _, err := rand.Read(bytes); err != nil {
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}
	randomString := hex.EncodeToString(bytes)
	AuthStateMap[randomString] = AuthInitInfo{
		UserID:   user.ID,
		Platform: in.Platform,
	}

	g.IndentedJSON(http.StatusOK, gin.H{
		"redirect_to": oauthConfig.AuthCodeURL(randomString),
	})

	return nil
}

func AuthCallback(g *gin.Context) error {
	reqState, ok := g.GetQuery("state")
	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	authInfo, ok := AuthStateMap[reqState]
	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	codeState, ok := g.GetQuery("code")
	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	token, err := oauthConfig.Exchange(context.Background(), codeState)

	if err != nil {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	scope, ok := scopeStringFromToken(token)
	if !ok {
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}

	model := &ProviderGithubAuthData{
		UserID:      authInfo.UserID,
		AccessToken: token.AccessToken,
		Scope:       scope,
	}

	if rst := initializers.DB.Create(&model); rst.Error != nil {
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}

	var redirectUrl string
	if authInfo.Platform == "web" {
		redirectUrl = os.Getenv("PROVIDER_OAUTH2_CALLBACK_URL_WEB")
	} else if authInfo.Platform == "mobile" {
		redirectUrl = os.Getenv("PROVIDER_OAUTH2_CALLBACK_URI_MOBILE")
	} else {
		return errors.BadRequest
	}
	g.Redirect(http.StatusTemporaryRedirect, redirectUrl)
	return nil
}

func AuthCheck(g *gin.Context) (*routes.ThirdPartyAuthCheck, error) {
	maybeUser, ok := g.Get("user")

	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	var count int64
	if rst := initializers.DB.
		Model(&ProviderGithubAuthData{}).
		Where("user_id=?", user.ID).
		Count(&count); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}

	return &routes.ThirdPartyAuthCheck{
		IsConnected: count >= 1,
	}, nil
}
