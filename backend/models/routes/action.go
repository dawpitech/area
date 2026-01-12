package routes

import "dawpitech/area/models"

type RequestGetActionInfo struct {
	ActionName string `path:"name"`
}

type ResponseGetActionInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []models.Parameter
	Outputs     []models.Parameter
}

type GetAllActionResponse struct {
	ActionsName []string
}
