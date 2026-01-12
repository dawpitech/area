package models

type Handler func(Context) error

type Context struct {
	OwnerUserID        uint
	WorkflowID         uint
	ActionName         string
	ActionParameters   map[string]string
	ModifierName       string
	ModifierParameters map[string]string
	ModifierHandler    Handler
	ReactionName       string
	ReactionParameters map[string]string
	ReactionHandler    Handler
}

type Parameter struct {
	Name       string
	PrettyName string
}

type Authentification struct {
	HandlerAuthInit     interface{}
	HandlerAuthCallback interface{}
	HandlerAuthCheck    interface{}
}

type Action struct {
	Name          string
	PrettyName    string
	Description   string
	Parameters    []Parameter
	Outputs       []Parameter
	SetupTrigger  Handler
	RemoveTrigger Handler
}

type Modifier struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []Parameter
	Outputs     []Parameter
	Handler     Handler
}

type Reaction struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []Parameter
	Handler     Handler
}

type Service struct {
	Name       string
	Actions    []Action
	Modifiers  []Modifier
	Reactions  []Reaction
	AuthMethod *Authentification
	DBModels   []interface{}
}
