package models

type HandlerCallback func(int) int

type Trigger struct {
}

type Action struct {
	Name string
}

type Reaction struct {
	Name    string
	Handler HandlerCallback
}

type Provider struct {
	Name string
	Icon string
}
