package routes

type WorkflowID struct {
	WorkflowID uint `path:"id"`
}

type EditWorkflowRequest struct {
	WorkflowID         uint `path:"id"`
	ActionName         string
	ActionParameters   []string
	ReactionName       string
	ReactionParameters []string
	Active             bool
}
