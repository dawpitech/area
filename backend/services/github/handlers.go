package github

import (
	"bytes"
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"encoding/json"
	"fmt"
	"github.com/juju/errors"
	"io"
	"log"
	"net/http"
)

type IssueRequest struct {
	Title     string   `json:"title"`
	Body      string   `json:"body"`
	Assignees []string `json:"assignees,omitempty"`
	Milestone int      `json:"milestone,omitempty"`
	Labels    []string `json:"labels,omitempty"`
}

func HandlerCreateAnIssue(ctx models.Context) error {
	var count int64
	if rst := initializers.DB.
		Model(&ProviderGithubAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		return errors.New("Internal server error.")
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Github Account linked, a github action cannot be used.")
		return errors.New("The user has not github account linked.")
	}

	target, targetOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "target_repository", ctx)
	issueName, issueNameOK := workflowEngine.GetParam(workflowEngine.ModifierHandler, "issue_name", ctx)
	issueContent, issueContentOK := workflowEngine.GetParam(workflowEngine.ModifierHandler, "issue_content", ctx)

	if !(targetOK || issueNameOK || issueContentOK) {
		return errors.New("Missing parameters")
	}

	var OwnerOAuth2Access ProviderGithubAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		return errors.New("Workflow owner doesn't exist")
	}

	token := OwnerOAuth2Access.AccessToken

	reqBody := IssueRequest{
		Title: issueName,
		Body:  issueContent,
	}

	bodyBytes, err := json.Marshal(reqBody)
	if err != nil {
		return err
	}

	url := fmt.Sprintf("https://api.github.com/repos/%s/issues", target)
	req, err := http.NewRequest("POST", url, bytes.NewReader(bodyBytes))
	if err != nil {
		log.Print(err)
		return errors.New("Github API is not reachable")
	}

	req.Header.Set("Accept", "application/vnd.github+json")
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("X-GitHub-Api-Version", "2022-11-28")
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		log.Print(err)
		return errors.New("Github API is not reachable")
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err)
		}
	}(resp.Body)

	_, _ = io.ReadAll(resp.Body)
	fmt.Printf("Response status: %s\n", resp.Status)
	//fmt.Printf("Response body:\n%s\n", string(respBody))
	return nil
}
