package services

import (
	"dawpitech/area/middlewares"
	"dawpitech/area/models"
	"dawpitech/area/services/github"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/wI2L/fizz"
)

var Services = []models.Service{
	github.Provider,
}

func RegisterServiceRoutes(router *fizz.Fizz) {
	for i := 0; i < len(Services); i++ {
		service := Services[i]
		router.GET(
			service.AuthMethod.RouteAuthInit,
			[]fizz.OperationOption{},
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
