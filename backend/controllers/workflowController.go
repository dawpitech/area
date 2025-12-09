package controllers

import (
	"dawpitech/area/engine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"dawpitech/area/utils"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllWorkflows(_ *gin.Context) (*[]models.Workflow, error) {
	var workflows []models.Workflow
	if rst := initializers.DB.Find(&workflows); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	return &workflows, nil
}

func CreateNewWorkflow(c *gin.Context, in *routes.CreateNewWorkflowRequest) (*models.Workflow, error) {
	maybeUser, ok := c.Get("user")
	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	workflow := models.Workflow{
		OwnerUserID:        user.ID,
		ActionName:         in.ActionName,
		ActionParameters:   in.ActionParameters,
		ReactionName:       in.ReactionName,
		ReactionParameters: in.ReactionParameters,
	}
	if rst := initializers.DB.Create(&workflow); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	if err, ok := engine.RegisterNewWorkflow(workflow); !ok {
		log.Print(err.Error())
		initializers.DB.Delete(&workflow)
		return nil, err
	}
	return &workflow, nil
}
