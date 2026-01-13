package routes

import "dawpitech/area/models"

type RequestGetModifierInfo struct {
	ModifierName string `path:"name"`
}

type ResponseGetModifierInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []models.PublicParameter
	Outputs     []models.PublicParameter
}

type GetAllModifierResponse struct {
	ModifiersName []string
}
