package controllers

import (
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"dawpitech/area/stores"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllReaction(_ *gin.Context) (*routes.GetAllReactionResponse, error) {
	var response routes.GetAllReactionResponse
	for i := 0; i < len(stores.ReactionList); i++ {
		response.ReactionsName = append(response.ReactionsName, stores.ReactionList[i].Name)
	}
	return &response, nil
}

func GetReactionInfo(_ *gin.Context, in *routes.RequestGetReactionInfo) (*routes.ResponseGetReactionInfo, error) {
	reaction, ok := stores.ReactionStore[in.ReactionName]
	if !ok {
		log.Printf("Request for reaction info failed, reaction ('%s') not found.", in.ReactionName)
		return nil, errors.NotFound
	}

	params := make([]models.PublicParameter, len(reaction.Parameters))
	for i, parameter := range reaction.Parameters {
		params[i] = parameter.ToPublic()
	}

	return &routes.ResponseGetReactionInfo{
		Name:        reaction.Name,
		PrettyName:  reaction.PrettyName,
		Description: reaction.Description,
		Parameters:  params,
	}, nil
}
