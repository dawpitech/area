package github

import (
	"context"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"encoding/hex"
	"github.com/gin-gonic/gin"
	"golang.org/x/oauth2"
	"math/rand"
	"net/http"
	"strings"
)

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
	maybeUser, ok := g.Get("user")

	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	var user models.User
	switch u := maybeUser.(type) {
	case models.User:
		user = u
	case *models.User:
		user = *u
	default:
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}

	bytes := make([]byte, 16)
	if _, err := rand.Read(bytes); err != nil {
		g.AbortWithStatus(http.StatusInternalServerError)
		return nil
	}
	randomString := hex.EncodeToString(bytes)
	AuthStateMap[randomString] = user.ID

	g.Redirect(http.StatusTemporaryRedirect, oauthConfig.AuthCodeURL(randomString))
	return nil
}

func AuthCallback(g *gin.Context) error {
	reqState, ok := g.GetQuery("state")
	if !ok {
		g.AbortWithStatus(http.StatusBadRequest)
		return nil
	}

	val, ok := AuthStateMap[reqState]
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
		UserID:       val,
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
