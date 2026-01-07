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

func GetWorkflow(_ *gin.Context, in *routes.WorkflowID) (*models.Workflow, error) {
	var workflow models.Workflow
	if rst := initializers.DB.First(&workflow).Where("id=?", in.WorkflowID); rst.Error != nil {
		return nil, errors.NotFound
	}
	return &workflow, nil
}

func CreateNewWorkflow(c *gin.Context) (*models.Workflow, error) {
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
		ActionName:         "",
		ActionParameters:   nil,
		ReactionName:       "",
		ReactionParameters: nil,
		Active:             false,
	}
	if rst := initializers.DB.Create(&workflow); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	return &workflow, nil
}

func CheckWorkflow(_ *gin.Context, in *routes.CheckWorkflowRequest) (*routes.CheckWorkflowResponse, error) {
	workflow := models.Workflow{
		//OwnerUserID:        0,
		ActionName:         in.ActionName,
		ActionParameters:   in.ActionParameters,
		ReactionName:       in.ReactionName,
		ReactionParameters: in.ReactionParameters,
		//Active:             false,
	}

	err, ok := engine.ValidateWorkflow(workflow)
	if !ok {
		return &routes.CheckWorkflowResponse{
			SyntaxValid: false,
			Error:       err.Error(),
		}, nil
	} else {
		return &routes.CheckWorkflowResponse{
			SyntaxValid: true,
			Error:       "",
		}, nil
	}
}

func EditWorkflow(c *gin.Context, in *routes.EditWorkflowRequest) (*models.Workflow, error) {
	maybeUser, ok := c.Get("user")
	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	var workflow models.Workflow
	if rst := initializers.DB.First(&workflow).Where("id=?", in.WorkflowID); rst.Error != nil {
		return nil, errors.New("No workflow found with the given ID.")
	}

	if workflow.OwnerUserID != user.ID {
		return nil, errors.Unauthorized
	}

	if workflow.Active {
		if err, ok := engine.DisableWorkflowTrigger(workflow); !ok {
			log.Print(err.Error())
			return nil, err
		}
	}

	workflow.ActionName = in.ActionName
	workflow.ActionParameters = in.ActionParameters
	workflow.ReactionName = in.ReactionName
	workflow.ReactionParameters = in.ReactionParameters
	workflow.Active = in.Active

	if rst := initializers.DB.Save(&workflow); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	if in.Active {
		if err, ok := engine.SetupWorkflowTrigger(workflow); !ok {
			log.Print(err.Error())
			//initializers.DB.Delete(&workflow)
			return nil, err
		}
	}

	return &workflow, nil
}
