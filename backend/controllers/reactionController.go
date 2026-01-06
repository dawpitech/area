package controllers

import (
	"dawpitech/area/models/routes"
	"dawpitech/area/services"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
)

func GetReactionInfo(_ *gin.Context, in *routes.RequestGetReactionInfo) (*routes.ResponseGetReactionInfo, error) {
	reaction, ok := services.ReactionStore[in.ReactionName]
	if !ok {
		return nil, errors.NotFound
	}

	return &routes.ResponseGetReactionInfo{
		Name:        reaction.Name,
		PrettyName:  reaction.PrettyName,
		Description: reaction.Description,
		Parameters:  reaction.Parameters,
	}, nil
}
