package routes

type CreateNewWorkflowRequest struct {
	ActionName         string
	ActionParameters   []string
	ReactionName       string
	ReactionParameters []string
}
