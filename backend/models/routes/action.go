package routes

type RequestGetActionInfo struct {
	ActionName string `path:"name"`
}

type ResponseGetActionInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []string
}

type GetAllActionResponse struct {
	ActionsName []string
}
