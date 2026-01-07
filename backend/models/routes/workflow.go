package routes

type WorkflowID struct {
	WorkflowID uint `path:"id"`
}

type EditWorkflowRequest struct {
	WorkflowID         uint `path:"id"`
	ActionName         string
	ActionParameters   map[string]string
	ReactionName       string
	ReactionParameters map[string]string
	Active             bool
}

type CheckWorkflowRequest struct {
	ActionName         string
	ActionParameters   map[string]string
	ReactionName       string
	ReactionParameters map[string]string
}

type CheckWorkflowResponse struct {
	SyntaxValid bool
	Error       string
}
