package engines

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

func ValidateWorkflow(workflow models.Workflow) (error, bool) {
	action, actPresent := services.ActionStore[workflow.ActionName]
	if !actPresent {
		return errors.New("Provided action doesnt exist."), false
	}
	reaction, reaPresent := services.ReactionStore[workflow.ReactionName]
	if !reaPresent {
		return errors.New("Provided reaction doesnt exist."), false
	}
	for i := 0; i < len(action.Parameters); i++ {
		_, ok := workflow.ActionParameters[action.Parameters[i]]
		if !ok {
			return errors.New("Not enough parameters given to chosen action."), false
		}
	}
	for i := 0; i < len(reaction.Parameters); i++ {
		_, ok := workflow.ReactionParameters[reaction.Parameters[i]]
		if !ok {
			return errors.New("Not enough parameters given to chosen reaction."), false
		}
	}
	return nil, true
}

func SetupWorkflowTrigger(workflow models.Workflow) (error, bool) {
	context := models.TriggerContext{
		OwnerUserID:      workflow.OwnerUserID,
		WorkflowID:       workflow.ID,
		ActionParameters: workflow.ActionParameters,
		ReactionContext: models.HandlerContext{
			OwnerUserID:        workflow.OwnerUserID,
			WorkflowID:         workflow.ID,
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
		return errors.New("Err occurred during setup of the trigger: " + err.Error()), false
	}
	return nil, true
}

func ReloadWorkflowTrigger() {
	var savedWorkflows []models.Workflow

	if rst := initializers.DB.Find(&savedWorkflows); rst.Error != nil {
		if errors.Is(rst.Error, gorm.ErrRecordNotFound) {
			log.Println("No workflows already saved, skipping re-init.")
			return
		}
		log.Fatal("Couldn't re-init saved workflows, exiting..")
	}

	for i := 0; i < len(savedWorkflows); i++ {
		err, ok := SetupWorkflowTrigger(savedWorkflows[i])
		if !ok {
			log.Print(err.Error())
		}
	}
}

func DisableWorkflowTrigger(workflow models.Workflow) (error, bool) {
	context := models.TriggerContext{
		OwnerUserID:      workflow.OwnerUserID,
		WorkflowID:       workflow.ID,
		ActionParameters: workflow.ActionParameters,
		ReactionContext: models.HandlerContext{
			OwnerUserID:        workflow.OwnerUserID,
			WorkflowID:         workflow.ID,
			ReactionParameters: workflow.ReactionParameters,
		},
		ReactionHandler: services.ReactionStore[workflow.ReactionName].Handler,
	}

	err := services.ActionStore[workflow.ActionName].RemoveTrigger(context)
	if err != nil {
		return errors.New("Removal of trigger failed, please re-try later."), false
	}
	return nil, true
}
