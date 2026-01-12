package controllers

import (
	"dawpitech/area/models/routes"
	"dawpitech/area/services"
	"github.com/gin-gonic/gin"
	"time"
)

func GetAbout(c *gin.Context) (*routes.AboutResponse, error) {
	var servs []routes.AboutService
	for i := 0; i < len(services.Services); i++ {
		service := services.Services[i]
		if service.Hidden {
			continue
		}

		var acts []routes.AboutServiceDetail
		var reacts []routes.AboutServiceDetail
		var mods []routes.AboutServiceDetail

		for y := 0; y < len(service.Actions); y++ {
			act := service.Actions[y]
			acts = append(acts, routes.AboutServiceDetail{
				Name:        act.PrettyName,
				Description: act.Description,
			})
		}
		for y := 0; y < len(service.Modifiers); y++ {
			mod := service.Modifiers[y]
			mods = append(mods, routes.AboutServiceDetail{
				Name:        mod.PrettyName,
				Description: mod.Description,
			})
		}
		for y := 0; y < len(service.Reactions); y++ {
			react := service.Reactions[y]
			reacts = append(reacts, routes.AboutServiceDetail{
				Name:        react.PrettyName,
				Description: react.Description,
			})
		}

		servs = append(servs, routes.AboutService{
			Name:      service.Name,
			Actions:   acts,
			Modifiers: mods,
			Reactions: reacts,
		})
	}

	return &routes.AboutResponse{
		Client: routes.AboutClient{
			Host: c.ClientIP(),
		},
		Server: routes.AboutServer{
			CurrentTime: time.Now().Unix(),
		},
		Services: servs,
	}, nil
}
