package routes

type WorkflowID struct {
	WorkflowID uint `path:"id"`
}

type GetAllWorkflowResponse struct {
	WorkflowID uint
	Name       string
	Active     bool
}

type GetWorkflowResponse struct {
	WorkflowID         uint
	Name               string
	ActionName         string
	ActionParameters   map[string]string `gorm:"serializer:json"`
	ReactionName       string
	ReactionParameters map[string]string `gorm:"serializer:json"`
	Active             bool
}

type EditWorkflowRequest struct {
	WorkflowID         uint `path:"id"`
	Name               string
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
