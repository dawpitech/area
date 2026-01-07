package timer

import (
	"dawpitech/area/models"
	"github.com/go-co-op/gocron/v2"
	"github.com/google/uuid"
	"log"
)

var scheduler gocron.Scheduler
var workflowJobUUID = make(map[uint]uuid.UUID)

func init() {
	var err error
	if scheduler, err = gocron.NewScheduler(); err != nil {
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
			Description: "Trigger a workflow every x amount of time",
			Parameters: []string{
				"cron",
			},
			SetupTrigger:  TriggerLaunchNewCronJob,
			RemoveTrigger: RemoveLaunchNewCronJob,
		}, /*
			{
				Name:        "timer_precise_run",
				PrettyName:  "Execute at",
				Description: "Trigger a workflow at a specific time in the day, format is (hour:min:sec); ex: (18:30:59)",
				Parameters: []string{
					"time",
				},
				SetupTrigger: TriggerLaunchAtJob,
			},*/
	},
	Reactions:  []models.Reaction{},
	AuthMethod: nil,
	DBModels:   []interface{}{},
}
