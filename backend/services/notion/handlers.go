package notion

import (
	"bytes"
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"

	"github.com/juju/errors"
)

type RichText struct {
	Text struct {
		Content string `json:"content"`
	} `json:"text"`
}

type CommentRequest struct {
	DiscID   string     `json:"discussion_id"`
	RichText []RichText `json:"rich_text"`
}

func HandlerNotionRespondToThread(ctx models.Context) error {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderNotionAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		return errors.New("Internal server error.")
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Notion Account linked, a notion comment cannot be sent.")
		return errors.New("The user has no notion account linked.")
	}

	target, targetOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "discussion_id", ctx)
	commentContent, commentContentOK := workflowEngine.GetParam(workflowEngine.ModifierHandler, "comment_content", ctx)

	if !(targetOK || commentContentOK) {
		return errors.New("Missing parameters")
	}

	var OwnerOAuth2Access ProviderNotionAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		return errors.New("Workflow owner doesn't exist")
	}

	token := OwnerOAuth2Access.AccessToken

	reqBody := CommentRequest{
		DiscID: target,
		RichText: []RichText{
			{
				Text: struct {
					Content string `json:"content"`
				}{
					Content: commentContent,
				},
			},
		},
	}

	bodyBytes, err := json.Marshal(reqBody)
	if err != nil {
		log.Print(err.Error())
	}
	log.Print("commentContent: ", commentContent)
	log.Print(string(bodyBytes))

	url := "https://api.notion.com/v1/comments"
	req, err := http.NewRequest("POST", url, bytes.NewReader(bodyBytes))
	if err != nil {
		log.Print(err)
		return errors.New("Notion API is not reachable")
	}

	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("Notion-Version", "2022-06-28")
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		log.Print(err)
		return errors.New("Notion API is not reachable")
	}

	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err)
		}
	}(resp.Body)

	_, _ = io.ReadAll(resp.Body)
	fmt.Printf("Response status: %s\n", resp.Status)
	return nil
}
