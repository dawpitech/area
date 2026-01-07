package services

import (
	"dawpitech/area/middlewares"
	"dawpitech/area/models"
	"dawpitech/area/services/github"
	"dawpitech/area/services/placeholder"
	"dawpitech/area/services/timer"
	"fmt"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/wI2L/fizz"
	"github.com/wI2L/fizz/openapi"
	"strings"
)

var Services = []models.Service{
	github.Provider,
	timer.Provider,
	placeholder.Provider,
}

var ActionStore = make(map[string]models.Action)
var ReactionStore = make(map[string]models.Reaction)

var ActionList []models.Action
var ReactionList []models.Reaction

func Init() {
	for i := 0; i < len(Services); i++ {
		service := Services[i]
		for x := 0; x < len(service.Actions); x++ {
			ActionStore[service.Actions[x].Name] = service.Actions[x]
			ActionList = append(ActionList, service.Actions[x])
		}
		for x := 0; x < len(service.Reactions); x++ {
			ReactionStore[service.Reactions[x].Name] = service.Reactions[x]
			ReactionList = append(ReactionList, service.Reactions[x])
		}
	}
}

func RegisterServiceRoutes(router *fizz.Fizz) {
	providersRoute := router.Group("", "Providers specific routes", "WIP")
	for i := 0; i < len(Services); i++ {
		service := Services[i]
		routeBase := fmt.Sprintf("/providers/%s/auth", strings.ToLower(service.Name))
		routeAuthInit := routeBase + "/init"
		routeAuthCallback := routeBase + "/callback"
		routeAuthCheck := routeBase + "/check"
		if service.AuthMethod != nil {
			providersRoute.GET(
				routeAuthInit,
				[]fizz.OperationOption{
					fizz.Security(&openapi.SecurityRequirement{
						"bearerAuth": []string{},
					}),
				},
				middlewares.CheckAuth,
				tonic.Handler(service.AuthMethod.HandlerAuthInit, 200),
			)
			providersRoute.GET(
				routeAuthCallback,
				[]fizz.OperationOption{},
				tonic.Handler(service.AuthMethod.HandlerAuthCallback, 200),
			)
			providersRoute.GET(
				routeAuthCheck,
				[]fizz.OperationOption{
					fizz.Security(&openapi.SecurityRequirement{
						"bearerAuth": []string{},
					}),
				},
				middlewares.CheckAuth,
				tonic.Handler(service.AuthMethod.HandlerAuthCheck, 200),
			)
		}
	}
}
