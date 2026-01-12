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
				},
			},
			Outputs: []models.Parameter{
				{
					Name:       "chatgpt_output",
					PrettyName: "ChatGPT Output",
				},
			},
			Handler: HandlerAskChatGPT,
		},
	},
	Reactions:  nil,
	AuthMethod: nil,
	DBModels:   nil,
}
