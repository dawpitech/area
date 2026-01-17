package google

import (
	"context"
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"encoding/base64"
	"github.com/juju/errors"
	"golang.org/x/oauth2"
	"google.golang.org/api/calendar/v3"
	"google.golang.org/api/gmail/v1"
	"google.golang.org/api/option"
)

func HandlerEmptyTrash(ctx models.Context) error {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderGoogleAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		return errors.New("Internal server error.")
	}

	if count < 1 {
		return errors.New("The user has not google account linked.")
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
	srv, err := gmail.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		return errors.New(err.Error())
	}

	listCall := srv.Users.Messages.List("me").Q("in:trash")
	messagesResponse, err := listCall.Do()
	if err != nil {
		return errors.New("Failed to list trash messages: " + err.Error())
	}

	if messagesResponse.Messages != nil {
		for _, msg := range messagesResponse.Messages {
			err = srv.Users.Messages.Delete("me", msg.Id).Do()
			if err != nil {
				logEngine.NewLogEntry(ctx.WorkflowID, models.WarnLog, "Failed to delete message "+msg.Id+": "+err.Error())
				continue
			}
		}
	}

	logEngine.NewLogEntry(ctx.WorkflowID, models.InfoLog, "Gmail trash emptied successfully.")
	return nil
}

func HandlerSendEmail(ctx models.Context) error {
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

	target, targetOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_send_email_target", ctx)
	body, bodyOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_send_email_body", ctx)
	subject, subjectOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "google_send_email_subject", ctx)

	if !(targetOK || bodyOK || subjectOK) {
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
	srv, err := gmail.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		return errors.New(err.Error())
	}

	sender, err := getGmailAddress(client)

	if err != nil {
		return err
	}

	var msg gmail.Message
	rawMsg := []byte(
		"From:" + sender + "\r\n" +
			"To: " + target + "\r\n" +
			"Subject: " + subject + "\r\n\r\n" +
			body)

	msg.Raw = base64.URLEncoding.EncodeToString(rawMsg)

	_, err = srv.Users.Messages.Send("me", &msg).Do()
	if err != nil {
		return err
	}

	return nil
}

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
