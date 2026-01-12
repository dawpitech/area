package discord_webhook

import (
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/models"
	"github.com/gtuk/discordwebhook"
	"github.com/juju/errors"
)

func HandlerPostMsg(ctx models.Context) error {
	msgContent, msgOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "discord_wh_post_content", ctx)
	webHookUrl, urlOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "discord_wh_url", ctx)
	username, usrOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "discord_wh_username", ctx)
	avatarURL, avatarOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "discord_wh_avatar_url", ctx)

	if !(msgOK || urlOK || usrOK || avatarOK) {
		return errors.New("Missing parameters")
	}

	message := discordwebhook.Message{
		Username:  &username,
		AvatarUrl: &avatarURL,
		Content:   &msgContent,
	}

	return discordwebhook.SendMessage(webHookUrl, message)
}
