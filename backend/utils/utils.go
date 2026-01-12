package utils

import (
	"dawpitech/area/models"
	"golang.org/x/oauth2"
	"strings"
)

func MaybeGetUser(maybeUser any) (*models.User, bool) {
	switch u := maybeUser.(type) {
	case models.User:
		return &u, true
	case *models.User:
		return u, true
	default:
		return nil, false
	}
}

func OAuthScopeStringFromToken(token *oauth2.Token) (string, bool) {
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
