package models

type HandlerContext struct {
	OwnerUserID        uint
	WorkflowID         uint
	ReactionParameters map[string]string
}

type TriggerContext struct {
	OwnerUserID      uint
	WorkflowID       uint
	ActionParameters map[string]string
	ReactionContext  HandlerContext
	ReactionHandler  HandlerCallback
}

type HandlerCallback func(HandlerContext) error
type TriggerSetup func(TriggerContext) error

type Trigger struct {
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
	Parameters    []string
	SetupTrigger  TriggerSetup
	RemoveTrigger TriggerSetup
}

type Reaction struct {
	Name        string
	PrettyName  string
	Description string
	Parameters  []string
	Handler     HandlerCallback
}

type Service struct {
	Name       string
	Icon       string
	Actions    []Action
	Reactions  []Reaction
	AuthMethod *Authentification
	DBModels   []interface{}
}
