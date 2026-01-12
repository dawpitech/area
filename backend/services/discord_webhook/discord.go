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
				},
				{
					Name:       "discord_wh_url",
					PrettyName: "Webhook URL",
				},
				{
					Name:       "discord_wh_username",
					PrettyName: "Webhook username",
				},
				{
					Name:       "discord_wh_avatar_url",
					PrettyName: "Webhook avatar URL",
				},
			},
			Handler: HandlerPostMsg,
		},
	},

	AuthMethod: nil,
	DBModels:   nil,
}
