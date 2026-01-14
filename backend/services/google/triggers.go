package google

import (
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"github.com/go-co-op/gocron/v2"
	"github.com/google/uuid"
	"github.com/juju/errors"
	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"google.golang.org/api/calendar/v3"
	"google.golang.org/api/option"
	"time"
)

var scheduler gocron.Scheduler
var workflowJobUUID = make(map[uint]uuid.UUID)

var KnownMeetingCooldownTable = make(map[uint]time.Time)

func RemoveIsInAMeeting(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func checkIsInAMeeting(ctx models.Context) {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderGoogleAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Google Account linked, a github action cannot be used.")
		return
	}

	var OwnerOAuth2Access ProviderGoogleAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	token := oauth2.Token{
		AccessToken: OwnerOAuth2Access.AccessToken,
		TokenType:   "Bearer",
	}

	_, present := KnownMeetingCooldownTable[ctx.WorkflowID]
	if present {
		if time.Now().Before(KnownMeetingCooldownTable[ctx.WorkflowID]) {
			return
		}
		delete(KnownMeetingCooldownTable, ctx.WorkflowID)
	}
	
	client := oauthConfig.Client(context.Background(), &token)
	srv, err := calendar.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, err.Error())
		return
	}

	item := calendar.FreeBusyRequestItem{
		Id: "primary",
	}

	currentTime := time.Now()

	timeMin := time.Date(
		currentTime.Year(),
		currentTime.Month(),
		currentTime.Day(),
		0, 0, 0, 0,
		currentTime.Location(),
	)
	timeMax := time.Date(
		currentTime.Year(),
		currentTime.Month(),
		currentTime.Day()+1,
		0, 0, 0, 0,
		currentTime.Location(),
	)

	freeBusyRequest := &calendar.FreeBusyRequest{
		Items: []*calendar.FreeBusyRequestItem{
			&item,
		},
		TimeMax:  timeMax.Format(time.RFC3339),
		TimeMin:  timeMin.Format(time.RFC3339),
		TimeZone: "Europe/Paris",
	}

	event, err := srv.Freebusy.Query(freeBusyRequest).Do()

	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, err.Error())
		return
	}

	freeBusyCalendar, exist := event.Calendars["primary"]
	if !exist {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Primary google freeBusyCalendar not found")
		return
	}

	if len(freeBusyCalendar.Busy) > 0 {
		now := time.Now()

		for _, busyPeriod := range freeBusyCalendar.Busy {
			startTime, err := time.Parse(time.RFC3339, busyPeriod.Start)
			if err != nil {
				logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse start time: "+err.Error())
				continue
			}

			endTime, err := time.Parse(time.RFC3339, busyPeriod.End)
			if err != nil {
				logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse end time: "+err.Error())
				continue
			}

			if now.After(startTime) && now.Before(endTime) {
				KnownMeetingCooldownTable[ctx.WorkflowID] = endTime
				workflowEngine.RunWorkflow(ctx)
				return
			}
		}

	}
}

func TriggerIsInAMeeting(ctx models.Context) error {
	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkIsInAMeeting, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}
