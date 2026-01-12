package controllers

import (
	"dawpitech/area/models/routes"
	"dawpitech/area/stores"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllModifier(_ *gin.Context) (*routes.GetAllModifierResponse, error) {
	var response routes.GetAllModifierResponse
	for i := 0; i < len(stores.ModifierList); i++ {
		response.ModifiersName = append(response.ModifiersName, stores.ModifierList[i].Name)
	}
	return &response, nil
}

func GetModifierInfo(_ *gin.Context, in *routes.RequestGetModifierInfo) (*routes.ResponseGetModifierInfo, error) {
	modifier, ok := stores.ModifierStore[in.ModifierName]
	if !ok {
		log.Printf("Request for action info failed, action ('%s') not found.", in.ModifierName)
		return nil, errors.NotFound
	}

	return &routes.ResponseGetModifierInfo{
		Name:        modifier.Name,
		PrettyName:  modifier.PrettyName,
		Description: modifier.Description,
		Parameters:  modifier.Parameters,
		Outputs:     modifier.Outputs,
	}, nil
}
