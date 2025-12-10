package engine

import (
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/services"
	"github.com/juju/errors"
	"gorm.io/gorm"
	"log"
)

type WorkflowEngine struct {
	RegisteredWorkflows []models.Workflow
	ActionTriggered     chan uint
}

func RegisterNewWorkflow(workflow models.Workflow) (error, bool) {
	context := models.TriggerContext{
		OwnerUserID:      workflow.OwnerUserID,
		ActionParameters: workflow.ActionParameters,
		ReactionContext: models.HandlerContext{
			OwnerUserID:        workflow.OwnerUserID,
			ReactionParameters: workflow.ReactionParameters,
		},
		ReactionHandler: services.ReactionStore[workflow.ReactionName].Handler,
	}
	_, ok := services.ActionStore[workflow.ActionName]
	if !ok {
		return errors.New("Provided action doesnt exist."), false
	}
	_, ok = services.ReactionStore[workflow.ReactionName]
	if !ok {
		return errors.New("Provided reaction doesnt exist."), false
	}
	err := services.ActionStore[workflow.ActionName].SetupTrigger(context)
	if err != nil {
		return errors.New("Action's trigger couldn't be set-up, please re-try later."), false
	}
	return nil, true
}

func RegisterExistingWorkflows() {
	var savedWorkflows []models.Workflow

	if rst := initializers.DB.Find(&savedWorkflows); rst.Error != nil {
		if errors.Is(rst.Error, gorm.ErrRecordNotFound) {
			log.Println("No workflows already saved, skipping re-init.")
			return
		}
		log.Fatal("Couldn't re-init saved workflows, exiting..")
	}

	for i := 0; i < len(savedWorkflows); i++ {
		workflow := savedWorkflows[i]
		context := models.TriggerContext{
			OwnerUserID:      workflow.OwnerUserID,
			ActionParameters: workflow.ActionParameters,
			ReactionContext: models.HandlerContext{
				OwnerUserID:        workflow.OwnerUserID,
				ReactionParameters: workflow.ReactionParameters,
			},
			ReactionHandler: services.ReactionStore[workflow.ReactionName].Handler,
		}
		_, ok := services.ActionStore[workflow.ActionName]
		if !ok {
			log.Print("Provided action doesnt exist.")
			continue
		}
		_, ok = services.ReactionStore[workflow.ReactionName]
		if !ok {
			log.Print("Provided reaction doesnt exist.")
			continue
		}
		err := services.ActionStore[workflow.ActionName].SetupTrigger(context)
		if err != nil {
			log.Print("Action's trigger couldn't be set-up, please re-try later.")
		}
	}
}
