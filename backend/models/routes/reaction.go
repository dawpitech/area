package routes

import "dawpitech/area/models"

type RequestGetReactionInfo struct {
	ReactionName string `path:"name"`
}

type ResponseGetReactionInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []models.PublicParameter
}

type GetAllReactionResponse struct {
	ReactionsName []string
}
