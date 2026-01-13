package workflowEngine

import (
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/stores"
	"github.com/juju/errors"
	"gorm.io/gorm"
	"log"
)

type HandlerType int

const (
	Trigger = iota
	ModifierHandler
	ReactionHandler
)

type WorkflowEngine struct {
	RegisteredWorkflows []models.Workflow
	ActionTriggered     chan uint
}

func ValidateWorkflow(workflow models.Workflow) (error, bool) {
	action, actPresent := stores.ActionStore[workflow.ActionName]
	if !actPresent {
		return errors.New("Provided action doesnt exist."), false
	}
	reaction, reaPresent := stores.ReactionStore[workflow.ReactionName]
	if !reaPresent {
		return errors.New("Provided reaction doesnt exist."), false
	}
	modifier, modPresent := stores.ModifierStore[workflow.ModifierName]
	if !modPresent {
		return errors.New("Provided modifier doesnt exist."), false
	}

	for i := 0; i < len(action.Parameters); i++ {
		_, ok := workflow.ActionParameters[action.Parameters[i].Name]
		if !ok {
			return errors.New("Not enough parameters given to chosen action."), false
		}
	}
	for i := 0; i < len(modifier.Parameters); i++ {
		_, ok := workflow.ModifierParameters[modifier.Parameters[i].Name]
		if !ok {
			return errors.New("Not enough parameters given to chosen modifier."), false
		}
	}
	for i := 0; i < len(reaction.Parameters); i++ {
		_, ok := workflow.ReactionParameters[reaction.Parameters[i].Name]
		if !ok {
			return errors.New("Not enough parameters given to chosen reaction."), false
		}
	}

	return nil, true
}

func SetupWorkflowTrigger(workflow models.Workflow) (error, bool) {
	context := models.Context{
		OwnerUserID:        workflow.OwnerUserID,
		WorkflowID:         workflow.ID,
		ActionName:         workflow.ActionName,
		ActionParameters:   workflow.ActionParameters,
		ModifierName:       workflow.ModifierName,
		ModifierParameters: workflow.ModifierParameters,
		ModifierHandler:    stores.ModifierStore[workflow.ModifierName].Handler,
		ReactionName:       workflow.ReactionName,
		ReactionParameters: workflow.ReactionParameters,
		ReactionHandler:    stores.ReactionStore[workflow.ReactionName].Handler,
	}
	_, ok := stores.ActionStore[workflow.ActionName]
	if !ok {
		return errors.New("Provided action doesnt exist."), false
	}
	_, ok = stores.ReactionStore[workflow.ReactionName]
	if !ok {
		return errors.New("Provided reaction doesnt exist."), false
	}
	err := stores.ActionStore[workflow.ActionName].SetupTrigger(context)
	if err != nil {
		logEngine.NewLogEntry(workflow.ID, models.ErrorLog, err.Error())
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
		if !savedWorkflows[i].Active {
			continue
		}
		err, ok := SetupWorkflowTrigger(savedWorkflows[i])
		if !ok {
			log.Print(err.Error())
		}
	}
}

func DisableWorkflowTrigger(workflow models.Workflow) (error, bool) {
	context := models.Context{
		OwnerUserID:        workflow.OwnerUserID,
		WorkflowID:         workflow.ID,
		ActionName:         workflow.ActionName,
		ActionParameters:   workflow.ActionParameters,
		ModifierName:       workflow.ModifierName,
		ModifierParameters: workflow.ModifierParameters,

		ReactionName:       workflow.ReactionName,
		ReactionParameters: workflow.ReactionParameters,
		ReactionHandler:    stores.ReactionStore[workflow.ReactionName].Handler,
	}
	err := stores.ActionStore[workflow.ActionName].RemoveTrigger(context)
	if err != nil {
		return errors.New("Removal of trigger failed, please re-try later. Err: " + err.Error()), false
	}
	return nil, true
}

func RunWorkflow(ctx models.Context) {
	log.Printf("Workflow #%d was triggered.\n", ctx.WorkflowID)
	ctx.RuntimeData = make(map[string]string)
	err := ctx.ModifierHandler(ctx)
	if err != nil {
		log.Printf("Workflow #%d failed during the modifier.\n", ctx.WorkflowID)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Err during the modifier: "+err.Error())
		return
	}
	err = ctx.ReactionHandler(ctx)
	if err != nil {
		log.Printf("Workflow #%d failed during the reaction.\n", ctx.WorkflowID)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Err during the reaction: "+err.Error())
		return
	}
	logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "Workflow execution was successful.")
	log.Printf("Workflow #%d run was successful.\n", ctx.WorkflowID)
}

func GetParam(hdxType HandlerType, paramName string, ctx models.Context) (string, bool) {
	var value string
	var present bool
	switch hdxType {
	case Trigger:
		value, present = ctx.ActionParameters[paramName]
		break
	case ModifierHandler:
		value, present = ctx.ModifierParameters[paramName]
		break
	case ReactionHandler:
		value, present = ctx.ReactionParameters[paramName]
		break
	default:
		log.Panic("Unknown HandlerType received")
	}

	if !present {
		return "", false
	}

	if len(value) == 0 || value == "#" {
		return "", false
	}

	if value[0] == '#' {
		value, present = ctx.RuntimeData[paramName[1:]]
		return value, present
	}

	return value, present
}
