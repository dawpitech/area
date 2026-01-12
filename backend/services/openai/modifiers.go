package openai

import (
	"context"
	"dawpitech/area/models"
	"github.com/openai/openai-go"
	"github.com/openai/openai-go/option"
	"github.com/openai/openai-go/responses"
	"os"
)

func HandlerAskChatGPT(ctx models.Context) error {
	openAIClient := openai.NewClient(option.WithAPIKey(os.Getenv("OPENAI_API_KEY")))

	//prompt := "Tu es Atoine (Atoine pas Antoine, soit précis) le pirate, fais une blague de pirate, tu dois faire le maximum pour être drôle mais rentrer dans ton rôle de pirate, fais une blague de pirate, tu peux commencer tes phrases par Arr, mais met l'accent sur ton côté pirate blagueur. N'hésite pas à rappeler dans ta blague qui tu es, à te présenter."
	prompt := ctx.ModifierParameters["chatgpt_prompt"]

	response, err := openAIClient.Responses.New(
		context.Background(),
		responses.ResponseNewParams{
			Input: responses.ResponseNewParamsInputUnion{OfString: openai.String(prompt)},
			Model: openai.ChatModelGPT4,
		},
	)
	if err != nil {
		return err
	}
	ctx.RuntimeData["chatgpt_output"] = response.OutputText()
	return nil
}
