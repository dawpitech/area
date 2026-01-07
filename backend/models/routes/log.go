package routes

import "dawpitech/area/models"

type GetAllLogsByWorkflowResponse struct {
	Logs []models.PublicLogEntry
}
