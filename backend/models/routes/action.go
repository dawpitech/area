package routes

import "dawpitech/area/models"

type RequestGetActionInfo struct {
	ActionName string `path:"name"`
}

type ResponseGetActionInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []models.PublicParameter
	Outputs     []models.PublicParameter
}

type GetAllActionResponse struct {
	ActionsName []string
}
