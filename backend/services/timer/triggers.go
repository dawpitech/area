package timer

import (
	"dawpitech/area/models"
	"github.com/go-co-op/gocron/v2"
	"github.com/juju/errors"
)

func TriggerLaunchNewCronJob(ctx models.TriggerContext) error {
	crontab := ctx.ActionParameters[0]
	_, err := scheduler.NewJob(
		gocron.CronJob(crontab, false),
		gocron.NewTask(
			ctx.ReactionHandler,
			ctx.ReactionContext,
		),
	)

	if err != nil {
		return errors.New("Set-up of the cron-job failed, please re-try later.")
	}

	return nil

}
