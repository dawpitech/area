package google

import (
	"dawpitech/area/models"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/calendar/v3"
	"os"
)

var Provider = models.Service{
	Name:      "Google",
	Hidden:    false,
	Actions:   nil,
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
				},
				{
					Name:       "google_create_event_desc",
					PrettyName: "Event description",
				},
				{
					Name:       "google_create_event_loc",
					PrettyName: "Event localisation",
				},
				{
					Name:       "google_create_event_start_date",
					PrettyName: "Event start time",
				},
				{
					Name:       "google_create_event_end_date",
					PrettyName: "Event end time",
				},
			},
			Handler: HandlerNewCalendarEvent,
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
	},
}
