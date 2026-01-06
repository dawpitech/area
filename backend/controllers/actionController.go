package controllers

import (
	"dawpitech/area/models/routes"
	"dawpitech/area/services"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetActionInfo(_ *gin.Context, in *routes.RequestGetActionInfo) (*routes.ResponseGetActionInfo, error) {
	action, ok := services.ActionStore[in.ActionName]
	if !ok {
		log.Printf("Request for action info failed, action ('%s') not found.", in.ActionName)
		return nil, errors.NotFound
	}

	return &routes.ResponseGetActionInfo{
		Name:        action.Name,
		PrettyName:  action.PrettyName,
		Description: action.Description,
		Parameters:  action.Parameters,
	}, nil
}
