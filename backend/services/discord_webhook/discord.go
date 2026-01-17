package discord_webhook

import "dawpitech/area/models"

var Provider = models.Service{
	Name:      "Discord WebHook",
	Hidden:    false,
	Actions:   nil,
	Modifiers: nil,
	Reactions: []models.Reaction{
		{
			Name:        "discord_webhook_post",
			PrettyName:  "Discord Post",
			Description: "Post a message on Discord via a WebHook",
			Parameters: []models.Parameter{
				{
					Name:       "discord_wh_post_content",
					PrettyName: "Message content",
					Type:       models.String,
				},
				{
					Name:       "discord_wh_url",
					PrettyName: "Webhook URL",
					Type:       models.String,
				},
				{
					Name:       "discord_wh_username",
					PrettyName: "Webhook username",
					Type:       models.String,
				},
				{
					Name:       "discord_wh_avatar_url",
					PrettyName: "Webhook avatar URL",
					Type:       models.String,
				},
			},
			Handler: HandlerPostMsg,
		},
	},
	AuthMethod:       nil,
	WebhookEndpoints: nil,
	DBModels:         nil,
}
