package github

import (
	"context"
	"dawpitech/area/initializers"
	"github.com/gin-gonic/gin"
	"golang.org/x/oauth2"
	"net/http"
	"strings"
)

var STATE = "random"

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

// route: /auth/github/init
func AuthInit(g *gin.Context) {
	g.Redirect(http.StatusTemporaryRedirect, oauthConfig.AuthCodeURL(STATE))
}

// route: /auth/github/callback
func AuthCallback(g *gin.Context) {
	if reqState, ok := g.GetQuery("state"); !ok || reqState != STATE {
		g.AbortWithStatus(http.StatusBadRequest)
		return
	}

	codeState, ok := g.GetQuery("code")
	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return
	}

	token, err := oauthConfig.Exchange(context.Background(), codeState)

	if err != nil {
		g.AbortWithStatus(http.StatusBadRequest)
		return
	}

	scope, ok := scopeStringFromToken(token)
	if !ok {
		g.AbortWithStatus(http.StatusInternalServerError)
		return
	}

	if rst := initializers.DB.Create(ProviderGithubAuthData{
		UserID:       0,
		AccessToken:  token.AccessToken,
		RefreshToken: token.RefreshToken,
		TokenExpiry:  token.Expiry,
		Scope:        scope,
	}); rst.Error != nil {
		g.AbortWithStatus(http.StatusInternalServerError)
		return
	}
}
