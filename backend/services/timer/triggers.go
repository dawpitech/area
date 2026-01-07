package timer

import (
	"dawpitech/area/models"
	"github.com/go-co-op/gocron/v2"
	"github.com/juju/errors"
	"log"
	"strconv"
	"strings"
)

func TriggerLaunchAtJob(ctx models.TriggerContext) error {
	time := strings.Split(ctx.ActionParameters["cron"], ":")

	if len(time) != 3 {
		return errors.New("Provided time of the day is invalid")
	}

	hour, errH := strconv.Atoi(time[0])
	minute, errM := strconv.Atoi(time[1])
	second, errS := strconv.Atoi(time[2])

	if errH != nil || errM != nil || errS != nil {
		return errors.New("Provided time of the day is invalid")
	}

	job, err := scheduler.NewJob(
		gocron.DailyJob(
			1,
			gocron.NewAtTimes(
				gocron.NewAtTime(uint(hour), uint(minute), uint(second)),
			),
		),
		gocron.NewTask(
			ctx.ReactionHandler,
			ctx.ReactionContext,
		),
	)

	if err != nil {
		return errors.New("Set-up of the scheduled job failed, please re-try later.")
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}

func RemoveLaunchNewCronJob(ctx models.TriggerContext) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func TriggerLaunchNewCronJob(ctx models.TriggerContext) error {
	crontab := ctx.ActionParameters["cron"]
	_, err := scheduler.NewJob(
		gocron.CronJob(crontab, false),
		gocron.NewTask(
			func() {
				log.Println("Running")
				err := ctx.ReactionHandler(ctx.ReactionContext)
				if err != nil {
					log.Print(err.Error())
				}
			},
		),
	)

	if err != nil {
		return errors.New("Set-up of the cron-job failed, please re-try later.")
	}

	return nil

}
