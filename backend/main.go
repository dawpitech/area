package main

import (
	"dawpitech/area/controllers"
	"dawpitech/area/initializers"
	"dawpitech/area/services"
	"github.com/gin-gonic/gin"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/loopfz/gadgeto/tonic/utils/jujerr"
	"github.com/wI2L/fizz"
	"log"
)

func init() {
	initializers.LoadEnvironment()
	initializers.ConnectDB()
}

func main() {
	var router = gin.Default()
	if err := router.SetTrustedProxies(nil); err != nil {
		log.Fatalln(err.Error())
	}

	tonic.SetErrorHook(jujerr.ErrHook)
	fizzRouter := fizz.NewFromEngine(router)

	authRoutes := fizzRouter.Group("/auth", "Authentification", "WIP")
	authRoutes.POST(
		"/signup",
		[]fizz.OperationOption{
			fizz.Summary("Register a new user"),
		},
		tonic.Handler(controllers.CreateNewUser, 200),
	)
	authRoutes.POST(
		"/sign-in",
		[]fizz.OperationOption{
			fizz.Summary("Log-in"),
		},
		tonic.Handler(controllers.LoginUser, 200),
	)

	services.RegisterServiceRoutes(fizzRouter)

	if err := router.Run("0.0.0.0:24680"); err != nil {
		log.Fatalln(err.Error())
	}
}
