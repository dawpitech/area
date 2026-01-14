package controllers

import (
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"dawpitech/area/utils"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
	"strconv"
)

func GetAllWorkflows(c *gin.Context) (*[]routes.GetAllWorkflowResponse, error) {
	maybeUser, ok := c.Get("user")
	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	var workflows []models.Workflow
	if rst := initializers.DB.Find(&workflows); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	var response []routes.GetAllWorkflowResponse
	for i := 0; i < len(workflows); i++ {
		if workflows[i].OwnerUserID != user.ID {
			continue
		}
		response = append(response, routes.GetAllWorkflowResponse{
			WorkflowID: workflows[i].ID,
			Name:       workflows[i].Name,
			Active:     workflows[i].Active,
		})
	}
	return &response, nil
}

func GetWorkflow(c *gin.Context, in *routes.WorkflowID) (*routes.GetWorkflowResponse, error) {
	maybeUser, ok := c.Get("user")
	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	var workflow models.Workflow
	if rst := initializers.DB.Where("id=?", in.WorkflowID).First(&workflow); rst.Error != nil {
		return nil, errors.NotFound
	}
	if workflow.OwnerUserID != user.ID {
		return nil, errors.NotFound
	}
	return &routes.GetWorkflowResponse{
		WorkflowID:         workflow.ID,
		Name:               workflow.Name,
		ActionName:         workflow.ActionName,
		ActionParameters:   workflow.ActionParameters,
		ModifierName:       workflow.ModifierName,
		ModifierParameters: workflow.ModifierParameters,
		ReactionName:       workflow.ReactionName,
		ReactionParameters: workflow.ReactionParameters,
		Active:             workflow.Active,
	}, nil
}

func CreateNewWorkflow(c *gin.Context) (*routes.GetWorkflowResponse, error) {
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
		Name:               "New Workflow",
		ActionName:         "none_action",
		ActionParameters:   nil,
		ModifierName:       "none_modifier",
		ModifierParameters: nil,
		ReactionName:       "none_reaction",
		ReactionParameters: nil,
		Active:             false,
	}
	if rst := initializers.DB.Create(&workflow); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	workflow.Name = workflow.Name + " " + strconv.Itoa(int(workflow.ID))
	initializers.DB.Save(&workflow)
	return &routes.GetWorkflowResponse{
		WorkflowID:         workflow.ID,
		Name:               workflow.Name,
		ActionName:         workflow.ActionName,
		ActionParameters:   workflow.ActionParameters,
		ModifierName:       workflow.ModifierName,
		ModifierParameters: workflow.ModifierParameters,
		ReactionName:       workflow.ReactionName,
		ReactionParameters: workflow.ReactionParameters,
		Active:             workflow.Active,
	}, nil
}

func CheckWorkflow(_ *gin.Context, in *routes.CheckWorkflowRequest) (*routes.CheckWorkflowResponse, error) {
	workflow := models.Workflow{
		ActionName:         in.ActionName,
		ActionParameters:   in.ActionParameters,
		ModifierName:       in.ModifierName,
		ModifierParameters: in.ModifierParameters,
		ReactionName:       in.ReactionName,
		ReactionParameters: in.ReactionParameters,
	}

	err, ok := workflowEngine.ValidateWorkflow(workflow)
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

func DeleteWorkflow(c *gin.Context, in *routes.WorkflowID) error {
	maybeUser, ok := c.Get("user")
	if !ok {
		return errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return errors.BadRequest
	}

	var workflow models.Workflow
	if rst := initializers.DB.Where("id=?", in.WorkflowID).First(&workflow); rst.Error != nil {
		return errors.New("No workflow found with the given ID.")
	}

	if workflow.OwnerUserID != user.ID {
		return errors.Unauthorized
	}

	if workflow.Active {
		if err, ok := workflowEngine.DisableWorkflowTrigger(workflow); !ok {
			log.Print(err.Error())
			return err
		}
	}

	initializers.DB.Delete(&workflow)
	return nil
}

func EditWorkflow(c *gin.Context, in *routes.EditWorkflowRequest) (*routes.GetWorkflowResponse, error) {
	maybeUser, ok := c.Get("user")
	if !ok {
		return nil, errors.BadRequest
	}

	user, ok := utils.MaybeGetUser(maybeUser)
	if !ok {
		return nil, errors.BadRequest
	}

	var workflow models.Workflow
	if rst := initializers.DB.Where("id=?", in.WorkflowID).First(&workflow); rst.Error != nil {
		return nil, errors.New("No workflow found with the given ID.")
	}

	if workflow.OwnerUserID != user.ID {
		return nil, errors.Unauthorized
	}

	if workflow.Active {
		if err, ok := workflowEngine.DisableWorkflowTrigger(workflow); !ok {
			log.Print(err.Error())
			return nil, err
		}
	}

	workflow.Name = in.Name
	workflow.ActionName = in.ActionName
	workflow.ActionParameters = in.ActionParameters
	workflow.ModifierName = in.ModifierName
	workflow.ModifierParameters = in.ModifierParameters
	workflow.ReactionName = in.ReactionName
	workflow.ReactionParameters = in.ReactionParameters
	workflow.Active = in.Active

	if rst := initializers.DB.Save(&workflow); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	if in.Active {
		if err, ok := workflowEngine.SetupWorkflowTrigger(workflow); !ok {
			log.Print(err.Error())
			workflow.Active = false
			if rst := initializers.DB.Save(&workflow); rst.Error != nil {
				return nil, errors.New("Internal server error")
			}
			//initializers.DB.Delete(&workflow)
			return nil, err
		}
	}

	return &routes.GetWorkflowResponse{
		WorkflowID:         workflow.ID,
		Name:               workflow.Name,
		ActionName:         workflow.ActionName,
		ActionParameters:   workflow.ActionParameters,
		ModifierName:       workflow.ModifierName,
		ModifierParameters: workflow.ModifierParameters,
		ReactionName:       workflow.ReactionName,
		ReactionParameters: workflow.ReactionParameters,
		Active:             workflow.Active,
	}, nil
}
