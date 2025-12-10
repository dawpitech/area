package services

import (
	"dawpitech/area/middlewares"
	"dawpitech/area/models"
	"dawpitech/area/services/github"
	"dawpitech/area/services/timer"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/wI2L/fizz"
	"github.com/wI2L/fizz/openapi"
)

var Services = []models.Service{
	github.Provider,
	timer.Provider,
}

var ActionStore = make(map[string]models.Action)
var ReactionStore = make(map[string]models.Reaction)

func Init() {
	for i := 0; i < len(Services); i++ {
		service := Services[i]
		for x := 0; x < len(service.Actions); x++ {
			ActionStore[service.Actions[x].Name] = service.Actions[x]
		}
		for x := 0; x < len(service.Reactions); x++ {
			ReactionStore[service.Reactions[x].Name] = service.Reactions[x]
		}
	}
}

func RegisterServiceRoutes(router *fizz.Fizz) {
	for i := 0; i < len(Services); i++ {
		service := Services[i]
		if service.AuthMethod != nil {
			router.GET(
				service.AuthMethod.RouteAuthInit,
				[]fizz.OperationOption{
					fizz.Security(&openapi.SecurityRequirement{
						"bearerAuth": []string{},
					}),
				},
				middlewares.CheckAuth,
				tonic.Handler(service.AuthMethod.HandlerAuthInit, 200),
			)
			router.GET(
				service.AuthMethod.RouteAuthCallback,
				[]fizz.OperationOption{},
				tonic.Handler(service.AuthMethod.HandlerAuthCallback, 200),
			)
		}
	}
}
