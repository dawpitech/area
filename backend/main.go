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
	"github.com/wI2L/fizz/openapi"
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
		AllowOrigins:     []string{"http://localhost:3000", "http://localhost:8081", "http://localhost:8082"},
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

	fizzRouter.Generator().SetSecuritySchemes(map[string]*openapi.SecuritySchemeOrRef{
		"bearerAuth": {
			SecurityScheme: &openapi.SecurityScheme{
				Type:         "http",
				Scheme:       "bearer",
				BearerFormat: "JWT",
			},
		},
	})

	infos := &openapi.Info{
		Title:       "Area API",
		Description: "TODO",
		Version:     "0.1.0",
	}
	fizzRouter.GET("/openapi.json", nil, fizzRouter.OpenAPI(infos, "json"))

	services.RegisterServiceRoutes(fizzRouter)

	if err := router.Run("0.0.0.0:8080"); err != nil {
		log.Fatalln(err.Error())
	}
}
