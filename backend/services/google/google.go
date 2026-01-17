package google

import (
	"dawpitech/area/models"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/calendar/v3"
	"google.golang.org/api/gmail/v1"
	"os"
)

var Provider = models.Service{
	Name:   "Google",
	Hidden: false,
	Actions: []models.Action{
		{
			Name:          "google_is_in_a_meeting",
			PrettyName:    "Is in a meeting",
			Description:   "Trigger if you are in a meeting based on your google calendar agenda",
			Parameters:    nil,
			Outputs:       nil,
			SetupTrigger:  TriggerIsInAMeeting,
			RemoveTrigger: RemoveIsInAMeeting,
		},
		{
			Name:        "google_new_email_received",
			PrettyName:  "Receive a mail",
			Description: "Trigger when you receive a new email in your gmail inbox",
			Parameters:  nil,
			Outputs: []models.Parameter{
				{
					Name:       "google_new_email_sender",
					PrettyName: "Email sender",
					Type:       models.String,
				},
				{
					Name:       "google_new_email_subject",
					PrettyName: "Email subject",
					Type:       models.String,
				},
			},
			SetupTrigger:  TriggerNewEmailReceived,
			RemoveTrigger: RemoveNewEmailReceived,
		},
	},
	Modifiers: nil,
	Reactions: []models.Reaction{
		{
			Name:        "google_create_calendar_event",
			PrettyName:  "Create an event in Google Calendar",
			Description: "Create a new event in your linked google account calendar",
			Parameters: []models.Parameter{
				{
					Name:       "google_create_event_name",
					PrettyName: "Event Name",
					Type:       models.String,
				},
				{
					Name:       "google_create_event_desc",
					PrettyName: "Event description",
					Type:       models.String,
				},
				{
					Name:       "google_create_event_loc",
					PrettyName: "Event localisation",
					Type:       models.String,
				},
				{
					Name:       "google_create_event_start_date",
					PrettyName: "Event start time",
					Type:       models.Date,
				},
				{
					Name:       "google_create_event_end_date",
					PrettyName: "Event end time",
					Type:       models.Date,
				},
			},
			Handler: HandlerNewCalendarEvent,
		},
		{
			Name:        "google_send_email",
			PrettyName:  "Send email from Gmail",
			Description: "Send an e-mail from your gmail account",
			Parameters: []models.Parameter{
				{
					Name:       "google_send_email_target",
					PrettyName: "Target email address",
					Type:       models.String,
				},
				{
					Name:       "google_send_email_body",
					PrettyName: "Email body",
					Type:       models.String,
				},
				{
					Name:       "google_send_email_subject",
					PrettyName: "Email subject",
					Type:       models.String,
				},
			},
			Handler: HandlerSendEmail,
		},
		{
			Name:        "google_clear_gmail_trash",
			PrettyName:  "Clear the trash inbox",
			Description: "Empty out your trash inbox of your gmail account",
			Parameters:  nil,
			Handler:     HandlerEmptyTrash,
		},
	},
	AuthMethod: &models.Authentification{
		HandlerAuthInit:     AuthGoogleInit,
		HandlerAuthCallback: AuthGoogleCallback,
		HandlerAuthCheck:    AuthGoogleCheck,
	},
	DBModels: []interface{}{
		&ProviderGoogleAuthData{},
	},
}

var oauthConfig = &oauth2.Config{
	ClientID:     os.Getenv("GOOGLE_OAUTH2_CLIENT_ID"),
	ClientSecret: os.Getenv("GOOGLE_OAUTH2_CLIENT_SECRET"),
	Endpoint:     google.Endpoint,
	RedirectURL:  os.Getenv("PUBLIC_URL") + "/providers/google/auth/callback",
	Scopes: []string{
		calendar.CalendarScope,
		gmail.GmailReadonlyScope,
		gmail.GmailSendScope,
		"https://mail.google.com/", //Required for the delete action for some reason it isn't in the enum?
	},
}
