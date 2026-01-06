package routes

type RequestGetReactionInfo struct {
	ReactionName string `path:"name"`
}

type ResponseGetReactionInfo struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []string
}
