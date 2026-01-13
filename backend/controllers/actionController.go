package controllers

import (
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"dawpitech/area/stores"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllAction(_ *gin.Context) (*routes.GetAllActionResponse, error) {
	var response routes.GetAllActionResponse
	for i := 0; i < len(stores.ActionList); i++ {
		response.ActionsName = append(response.ActionsName, stores.ActionList[i].Name)
	}
	return &response, nil
}

func GetActionInfo(_ *gin.Context, in *routes.RequestGetActionInfo) (*routes.ResponseGetActionInfo, error) {
	action, ok := stores.ActionStore[in.ActionName]
	if !ok {
		log.Printf("Request for action info failed, action ('%s') not found.", in.ActionName)
		return nil, errors.NotFound
	}

	params := make([]models.PublicParameter, len(action.Parameters))
	for i, parameter := range action.Parameters {
		params[i] = parameter.ToPublic()
	}

	outputs := make([]models.PublicParameter, len(action.Outputs))
	for i, output := range action.Outputs {
		outputs[i] = output.ToPublic()
	}

	return &routes.ResponseGetActionInfo{
		Name:        action.Name,
		PrettyName:  action.PrettyName,
		Description: action.Description,
		Parameters:  params,
		Outputs:     outputs,
	}, nil
}
