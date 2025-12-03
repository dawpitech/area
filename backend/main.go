package main

import (
	"dawpitech/area/controllers"
	"dawpitech/area/initializers"
	"dawpitech/area/services"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/loopfz/gadgeto/tonic/utils/jujerr"
	"github.com/wI2L/fizz"
	"log"
	"time"
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

	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost:3000", "http://localhost:8081"},
		AllowMethods:     []string{"GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

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

	fizzRouter.GET(
		"/about.json",
		[]fizz.OperationOption{
			fizz.Summary("Retrieve about.json"),
		},
		tonic.Handler(controllers.GetAbout, 200),
	)

	services.RegisterServiceRoutes(fizzRouter)

	if err := router.Run("0.0.0.0:8080"); err != nil {
		log.Fatalln(err.Error())
	}
}
