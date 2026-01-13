package google

import (
	"context"
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"github.com/juju/errors"
	"golang.org/x/oauth2"
	"google.golang.org/api/calendar/v3"
	"google.golang.org/api/option"
)

func HandlerNewCalendarEvent(ctx models.Context) error {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderGoogleAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		return errors.New("Internal server error.")
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Google Account linked, a github action cannot be used.")
		return errors.New("The user has not google account linked.")
	}

	name, nameOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_create_event_name", ctx)
	desc, descOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_create_event_desc", ctx)
	loc, locOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_create_event_loc", ctx)
	startDate, startDateOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_create_event_start_date", ctx)
	endDate, endDateOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_create_event_end_date", ctx)

	if !(nameOK || descOK || locOK || startDateOK || endDateOK) {
		return errors.New("Missing parameters")
	}

	var OwnerOAuth2Access ProviderGoogleAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		return errors.New("Workflow owner doesn't exist")
	}

	token := oauth2.Token{
		AccessToken: OwnerOAuth2Access.AccessToken,
		TokenType:   "Bearer",
	}

	client := oauthConfig.Client(context.Background(), &token)
	srv, err := calendar.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		return errors.New(err.Error())
	}

	event := &calendar.Event{
		Summary:     name,
		Location:    loc,
		Description: desc,
		Start: &calendar.EventDateTime{
			DateTime: startDate, //"2025-01-14T07:00:00+01:00"
			TimeZone: "Europe/Paris",
		},
		End: &calendar.EventDateTime{
			DateTime: endDate, //"2025-01-14T09:00:00+01:00"
			TimeZone: "Europe/Paris",
		},
	}

	event, err = srv.Events.Insert("primary", event).Do()
	if err != nil {
		return errors.New(err.Error())
	}
	logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "Google Calendar event created: "+event.HtmlLink)
	return nil
}
