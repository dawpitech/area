package main

import (
	"dawpitech/area/controllers"
	"dawpitech/area/initializers"
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

	if err := router.Run("0.0.0.0:24680"); err != nil {
		log.Fatalln(err.Error())
	}
}
