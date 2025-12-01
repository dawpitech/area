package models

type HandlerCallback func() error

type Trigger struct {
}

type Action struct {
	Name       string
	Parameters []string
}

type Reaction struct {
	Name       string
	Parameters []string
	Handler    HandlerCallback
}

type Service struct {
	Name       string
	Icon       string
	Actions    []Action
	Reactions  []Reaction
	AuthMethod interface{}
	DBModels   []interface{}
}
