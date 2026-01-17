package models

import "github.com/gin-gonic/gin"

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
	RuntimeData        map[string]string
}

type PublicParameter struct {
	Name       string
	PrettyName string
	Type       string `validate:"oneof=string date"`
}

type ParameterType int

const (
	String = iota
	Date
)

var ParameterTypeName = map[ParameterType]string{
	String: "string",
	Date:   "date",
}

func (pType ParameterType) String() string {
	return ParameterTypeName[pType]
}

func (p Parameter) ToPublic() PublicParameter {
	return PublicParameter{
		Name:       p.Name,
		PrettyName: p.PrettyName,
		Type:       p.Type.String(),
	}
}

type Parameter struct {
	Name       string
	PrettyName string
	Type       ParameterType
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

type WebhookHandler func(gin.Context) error

type WebhookEndpoint struct {
	EndpointURL   string
	HandlerMethod WebhookHandler
}

type Service struct {
	Name             string
	Hidden           bool
	Actions          []Action
	Modifiers        []Modifier
	Reactions        []Reaction
	AuthMethod       *Authentification
	WebhookEndpoints []WebhookEndpoint
	DBModels         []interface{}
}
