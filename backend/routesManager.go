package main

import (
	"dawpitech/area/controllers"
	"dawpitech/area/middlewares"
	"dawpitech/area/services"
	"github.com/loopfz/gadgeto/tonic"
	"github.com/wI2L/fizz"
	"github.com/wI2L/fizz/openapi"
)

func RegisterRoutes(fizzRouter *fizz.Fizz) {
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

	miscRoutes := fizzRouter.Group("/", "Misc", "WIP")
	miscRoutes.GET(
		"/about.json",
		[]fizz.OperationOption{
			fizz.Summary("Retrieve about.json"),
		},
		tonic.Handler(controllers.GetAbout, 200),
	)

	workflowRoutes := fizzRouter.Group("/workflow", "Workflow", "WIP")
	workflowRoutes.GET(
		"/",
		[]fizz.OperationOption{
			fizz.Summary("Retrieve all workflows"),
		},
		tonic.Handler(controllers.GetAllWorkflows, 200),
	)
	workflowRoutes.POST(
		"/",
		[]fizz.OperationOption{
			fizz.Summary("Create a new workflow"),
			fizz.Security(&openapi.SecurityRequirement{
				"bearerAuth": []string{},
			}),
		},
		middlewares.CheckAuth,
		tonic.Handler(controllers.CreateNewWorkflow, 200),
	)
	workflowRoutes.POST(
		"/check",
		[]fizz.OperationOption{
			fizz.Summary("Check the syntax validity of the given workflow"),
		},
		tonic.Handler(controllers.CheckWorkflow, 200),
	)
	workflowRoutes.DELETE(
		"/:id",
		[]fizz.OperationOption{
			fizz.Summary("Delete a workflow"),
			fizz.Security(&openapi.SecurityRequirement{
				"bearerAuth": []string{},
			}),
		},
		middlewares.CheckAuth,
		tonic.Handler(controllers.DeleteWorkflow, 200),
	)
	workflowRoutes.GET(
		"/:id",
		[]fizz.OperationOption{
			fizz.Summary("Retrieve a workflow"),
		},
		tonic.Handler(controllers.GetWorkflow, 200),
	)
	workflowRoutes.PATCH(
		"/:id",
		[]fizz.OperationOption{
			fizz.Summary("Edit a workflow"),
			fizz.Security(&openapi.SecurityRequirement{
				"bearerAuth": []string{},
			}),
		},
		middlewares.CheckAuth,
		tonic.Handler(controllers.EditWorkflow, 200),
	)

	actionsRoutes := fizzRouter.Group("/action", "Actions details", "WIP")
	actionsRoutes.GET(
		"/",
		[]fizz.OperationOption{
			fizz.Summary("Get the list of all actions"),
		},
		tonic.Handler(controllers.GetAllAction, 200),
	)
	actionsRoutes.GET(
		"/:name",
		[]fizz.OperationOption{
			fizz.Summary("Get details about an action"),
		},
		tonic.Handler(controllers.GetActionInfo, 200),
	)

	reactionsRoutes := fizzRouter.Group("/reaction", "Reactions details", "WIP")
	reactionsRoutes.GET(
		"/",
		[]fizz.OperationOption{
			fizz.Summary("Get the list of all reactions"),
		},
		tonic.Handler(controllers.GetAllReaction, 200),
	)
	reactionsRoutes.GET(
		"/:name",
		[]fizz.OperationOption{
			fizz.Summary("Get details about a reaction"),
		},
		tonic.Handler(controllers.GetReactionInfo, 200),
	)

	modifiersRoutes := fizzRouter.Group("/modifiers", "Modifiers details", "WIP")
	modifiersRoutes.GET(
		"/",
		[]fizz.OperationOption{
			fizz.Summary("Get the list of all modifiers"),
		},
		tonic.Handler(controllers.GetAllModifier, 200),
	)
	modifiersRoutes.GET(
		"/:name",
		[]fizz.OperationOption{
			fizz.Summary("Get details about a modifier"),
		},
		tonic.Handler(controllers.GetModifierInfo, 200),
	)

	logsRoutes := fizzRouter.Group("/logs", "Logs engine", "WIP")
	logsRoutes.GET(
		"/workflow/:id",
		[]fizz.OperationOption{
			fizz.Summary("Retrieve logs for a workflow"),
		},
		tonic.Handler(controllers.GetAllLogsByWorkflow, 200),
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
		Version:     "0.2.0",
	}
	fizzRouter.GET("/openapi.json", nil, fizzRouter.OpenAPI(infos, "json"))

	services.RegisterServiceRoutes(fizzRouter)
}
