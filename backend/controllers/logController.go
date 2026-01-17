package controllers

import (
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"sort"
)

func GetAllLogsByWorkflow(_ *gin.Context, in *routes.WorkflowID) (*routes.GetAllLogsByWorkflowResponse, error) {
	var logs []models.LogEntry
	if rst := initializers.DB.Where("workflow_id=?", in.WorkflowID).Find(&logs); rst.Error != nil {
		return nil, errors.New("Internal server error.")
	}
	var response routes.GetAllLogsByWorkflowResponse
	for i := 0; i < len(logs); i++ {
		response.Logs = append(response.Logs, models.PublicLogEntry{
			Timestamp: logs[i].Timestamp,
			Type:      logs[i].Type,
			Message:   logs[i].Message,
		})
	}
	sort.Slice(response.Logs, func(i, j int) bool {
		return response.Logs[i].Timestamp.After(response.Logs[j].Timestamp)
	})
	return &response, nil
}
