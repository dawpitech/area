package logEngine

import (
	"context"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"github.com/juju/errors"
	"gorm.io/gorm"
	"log"
	"time"
)

func NewLogEntry(workflowID uint, logType models.LogType, msg string) {
	workflow, err := gorm.G[models.Workflow](initializers.DB).First(context.Background())
	if err != nil {
		if errors.Is(err, gorm.ErrRegistered) {
			log.Print("Tried to write a log entry for a workflow that doesn't exist!")
		} else {
			log.Print("Couldn't load workflow to write log entry. Err: " + err.Error())
		}
	}
 
	entry := models.LogEntry{
		WorkflowID:  workflowID,
		OwnerUserID: workflow.OwnerUserID,
		Timestamp:   time.Now(),
		Type:        logType.String(),
		Message:     msg,
	}
	rst := initializers.DB.Create(&entry)
	if rst.Error != nil {
		log.Print("Error occurred while saving log entry in db. Err: " + rst.Error.Error())
	}
}
