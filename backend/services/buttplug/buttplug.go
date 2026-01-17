package buttplug

import (
	"dawpitech/area/models"
)

var Provider = models.Service{
	Name:      "Buttplug",
	Hidden:    false,
	Actions:   nil,
	Modifiers: nil,
	Reactions: []models.Reaction{
		{
			Name:        "vibrate_device",
			PrettyName:  "Vibrate Device",
			Description: "Vibrate a connected Buttplug device",
			Parameters: []models.Parameter{
				{
					Name:       "buttplug_server_ip",
					PrettyName: "Buttplug.io websocket Server IP",
					Type:       models.String,
				},
				{
					Name:       "buttplug_vibrate_device_intensity",
					PrettyName: "Vibration Intensity (0 - 100)",
					Type:       models.String,
				},
				{
					Name:       "buttplug_vibrate_device_duration",
					PrettyName: "Vibration Duration (seconds)",
					Type:       models.String,
				},
			},
			Handler: VibrateHandler,
		},
	},
	AuthMethod:       nil,
	WebhookEndpoints: nil,
	DBModels:         nil,
}
