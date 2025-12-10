package models

type HandlerContext struct {
	OwnerUserID        uint
	ReactionParameters []string
}

type TriggerContext struct {
	OwnerUserID      uint
	ActionParameters []string
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
	Name         string
	PrettyName   string
	Description  string
	Parameters   []string
	SetupTrigger TriggerSetup
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
