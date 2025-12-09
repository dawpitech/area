package timer

import (
	"dawpitech/area/models"
	"github.com/go-co-op/gocron/v2"
	"log"
)

var scheduler, err = gocron.NewScheduler()

func init() {
	if err != nil {
		log.Panic("Module timer couldn't init a job scheduler")
	}
	scheduler.Start()
}

var Provider = models.Service{
	Name: "Timer",
	Icon: "",
	Actions: []models.Action{
		{
			Name:        "timer_cron_job",
			PrettyName:  "Repeat every",
			Description: "SetupTrigger a workflow every x amount of time",
			Parameters: []string{
				"cron",
			},
			SetupTrigger: TriggerLaunchNewCronJob,
		},
		{
			Name:        "timer_wip",
			PrettyName:  "(WIP) Execute at every",
			Description: "(WIP) SetupTrigger a workflow at a specific time in the day",
			Parameters:  nil,
		},
	},
	Reactions:  []models.Reaction{},
	AuthMethod: nil,
	DBModels:   []interface{}{},
}
