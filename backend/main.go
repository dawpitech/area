package main

import (
	"dawpitech/area/engines/workflowEngine"
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
	services.Init()
	workflowEngine.ReloadWorkflowTrigger()
}

func main() {
	var router = gin.Default()
	if err := router.SetTrustedProxies(nil); err != nil {
		log.Fatalln(err.Error())
	}

	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost:3000", "http://localhost:8081", "http://localhost:8082"},
		AllowMethods:     []string{"GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

	tonic.SetErrorHook(jujerr.ErrHook)
	fizzRouter := fizz.NewFromEngine(router)

	RegisterRoutes(fizzRouter)

	if err := router.Run("0.0.0.0:8080"); err != nil {
		log.Fatalln(err.Error())
	}
}
