package routes

import "dawpitech/area/models"

type RequestGetModifierInfo struct {
	ModifierName string `path:"name"`
}

type ResponseGetModifierInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []models.Parameter
	Outputs     []models.Parameter
}

type GetAllModifierResponse struct {
	ModifiersName []string
}
