package services

import (
	"dawpitech/area/models"
	"dawpitech/area/services/github"
)

var Services = []models.Service{
	github.Provider,
}
