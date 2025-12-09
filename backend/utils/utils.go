package utils

import (
	"dawpitech/area/models"
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
