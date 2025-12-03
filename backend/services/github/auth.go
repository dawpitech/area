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

func AuthInit(g *gin.Context) error {
	g.Redirect(http.StatusTemporaryRedirect, oauthConfig.AuthCodeURL(STATE))
	return nil
}

func AuthCallback(g *gin.Context) error {
	if reqState, ok := g.GetQuery("state"); !ok || reqState != STATE {
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
		UserID:       0,
		AccessToken:  token.AccessToken,
		RefreshToken: token.RefreshToken,
		TokenExpiry:  token.Expiry,
		Scope:        scope,
	}

	if rst := initializers.DB.Create(&model); rst.Error != nil {
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}
	return nil
}
