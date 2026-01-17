package openai

import "dawpitech/area/models"

var Provider = models.Service{
	Name:    "OpenAI",
	Hidden:  false,
	Actions: nil,
	Modifiers: []models.Modifier{
		{
			Name:        "openai_ask_chatgpt",
			PrettyName:  "Ask ChatGPT",
			Description: "Retrieve what chatgpt answer based on your prompt",
			Parameters: []models.Parameter{
				{
					Name:       "chatgpt_prompt",
					PrettyName: "Prompt",
					Type:       models.String,
				},
			},
			Outputs: []models.Parameter{
				{
					Name:       "chatgpt_output",
					PrettyName: "ChatGPT Output",
					Type:       models.String,
				},
			},
			Handler: HandlerAskChatGPT,
		},
	},
	Reactions:        nil,
	AuthMethod:       nil,
	WebhookEndpoints: nil,
	DBModels:         nil,
}
