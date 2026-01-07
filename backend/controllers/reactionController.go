package controllers

import (
	"dawpitech/area/models/routes"
	"dawpitech/area/services"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllReaction(_ *gin.Context) (*routes.GetAllReactionResponse, error) {
	var response routes.GetAllReactionResponse
	for i := 0; i < len(services.ReactionList); i++ {
		response.ReactionsName = append(response.ReactionsName, services.ReactionList[i].Name)
	}
	return &response, nil
}

func GetReactionInfo(_ *gin.Context, in *routes.RequestGetReactionInfo) (*routes.ResponseGetReactionInfo, error) {
	reaction, ok := services.ReactionStore[in.ReactionName]
	if !ok {
		log.Printf("Request for reaction info failed, reaction ('%s') not found.", in.ReactionName)
		return nil, errors.NotFound
	}

	return &routes.ResponseGetReactionInfo{
		Name:        reaction.Name,
		PrettyName:  reaction.PrettyName,
		Description: reaction.Description,
		Parameters:  reaction.Parameters,
	}, nil
}
