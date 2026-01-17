package github

import (
	"dawpitech/area/engines/logEngine"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"encoding/json"
	"fmt"
	"github.com/go-co-op/gocron/v2"
	"github.com/google/uuid"
	"github.com/juju/errors"
	"io"
	"log"
	"net/http"
	"time"
)

var scheduler gocron.Scheduler
var workflowJobUUID = make(map[uint]uuid.UUID)

func init() {
	var err error
	if scheduler, err = gocron.NewScheduler(); err != nil {
		log.Panic("Module github couldn't init a job scheduler")
	}
	scheduler.Start()
	if commitScheduler, err = gocron.NewScheduler(); err != nil {
		log.Panic("Module github couldn't init a commit job scheduler")
	}
	commitScheduler.Start()
}

type StarDetail struct {
	StarredAt string `json:"starred_at"`
	User      struct {
		Login string `json:"login"`
		ID    uint   `json:"id"`
	} `json:"user"`
}

var commitScheduler gocron.Scheduler
var commitWorkflowJobUUID = make(map[uint]uuid.UUID)
var lastCommitSHA = make(map[uint]string)

type CommitDetail struct {
	SHA    string `json:"sha"`
	Commit struct {
		Author struct {
			Name  string    `json:"name"`
			Email string    `json:"email"`
			Date  time.Time `json:"date"`
		} `json:"author"`
		Message string `json:"message"`
	} `json:"commit"`
	Author struct {
		Login string `json:"login"`
		ID    uint   `json:"id"`
	} `json:"author"`
}

func RemoveNewCommitOnRepo(ctx models.Context) error {
	err := commitScheduler.RemoveJob(commitWorkflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error.  Err " + err.Error())
	}
	delete(commitWorkflowJobUUID, ctx.WorkflowID)
	delete(lastCommitSHA, ctx.WorkflowID)
	return nil
}

func checkNewCommitOnRepo(ctx models.Context) {
	target, targetOK := workflowEngine.GetParam(workflowEngine.Trigger, "commit_target_repository", ctx)
	branch, branchOK := workflowEngine.GetParam(workflowEngine.Trigger, "commit_target_branch", ctx)

	if !targetOK || !branchOK {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Missing parameter is required.")
		return
	}

	var count int64
	if rst := initializers.DB.
		Model(&ProviderGithubAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Github Account linked, a github action cannot be used.")
		return
	}

	var OwnerOAuth2Access ProviderGithubAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	token := OwnerOAuth2Access.AccessToken

	url := fmt.Sprintf("https://api.github.com/repos/%s/commits?sha=%s&per_page=1", target, branch)
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		log.Print(err)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Github API is not reachable")
		return
	}

	req.Header.Set("Accept", "application/vnd.github+json")
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("X-GitHub-Api-Version", "2022-11-28")

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		log.Print(err)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Github API is not reachable")
		return
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err)
		}
	}(resp.Body)

	if resp.StatusCode != http.StatusOK {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, fmt.Sprintf("Github API error: %s", resp.Status))
		return
	}

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to read response body")
		return
	}

	var commits []CommitDetail
	if err := json.Unmarshal(respBody, &commits); err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse Github response")
		return
	}

	if len(commits) == 0 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.WarnLog, "No commits found for the specified branch")
		return
	}

	latestCommit := commits[0]

	log.Printf("Used commit hash %s as base, compared to %s\n", lastCommitSHA[ctx.WorkflowID][:7], latestCommit.SHA[:7])

	if lastCommitSHA[ctx.WorkflowID] != latestCommit.SHA {
		lastCommitSHA[ctx.WorkflowID] = latestCommit.SHA
		ctx.RuntimeData["github_new_commit_message"] = latestCommit.Commit.Message
		ctx.RuntimeData["github_new_commit_author"] = latestCommit.Commit.Author.Name
		workflowEngine.RunWorkflow(ctx)
	}
}

func TriggerNewCommitOnRepo(ctx models.Context) error {
	target, targetOK := workflowEngine.GetParam(workflowEngine.Trigger, "commit_target_repository", ctx)
	branch, branchOK := workflowEngine.GetParam(workflowEngine.Trigger, "commit_target_branch", ctx)

	if !targetOK || !branchOK {
		return errors.New("Missing required parameters: commit_target_repository or commit_target_branch")
	}

	var count int64
	if rst := initializers.DB.
		Model(&ProviderGithubAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		return errors.New("Internal server error")
	}

	if count < 1 {
		return errors.New("No Github Account linked, a github action cannot be used")
	}

	var OwnerOAuth2Access ProviderGithubAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		return errors.New("Internal server error")
	}

	token := OwnerOAuth2Access.AccessToken

	url := fmt.Sprintf("https://api.github.com/repos/%s/commits?sha=%s&per_page=1", target, branch)
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return errors.New("Github API is not reachable")
	}

	req.Header.Set("Accept", "application/vnd.github+json")
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("X-GitHub-Api-Version", "2022-11-28")

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		return errors.New("Github API is not reachable")
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err)
		}
	}(resp.Body)

	if resp.StatusCode != http.StatusOK {
		return errors.New(fmt.Sprintf("Github API error: %s", resp.Status))
	}

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return errors.New("Failed to read response body")
	}

	var commits []CommitDetail
	if err := json.Unmarshal(respBody, &commits); err != nil {
		return errors.New("Failed to parse Github response")
	}

	if len(commits) == 0 {
		lastCommitSHA[ctx.WorkflowID] = "fffffffffffffffffffffffffff"
	} else {
		lastCommitSHA[ctx.WorkflowID] = commits[0].SHA
	}

	log.Printf("Used commit hash %s as base\n", lastCommitSHA[ctx.WorkflowID][:7])

	job, err := commitScheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkNewCommitOnRepo, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	commitWorkflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}

func RemoveNewStarOnRepo(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func checkNewStarOnRepo(ctx models.Context) {
	target, targetOK := workflowEngine.GetParam(workflowEngine.Trigger, "star_target_repository", ctx)

	if !targetOK {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Missing parameters.")
		return
	}

	var count int64
	if rst := initializers.DB.
		Model(&ProviderGithubAuthData{}).
		Where("user_id=?", ctx.OwnerUserID).
		Count(&count); rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	if count < 1 {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "No Github Account linked, a github action cannot be used.")
		return
	}

	var OwnerOAuth2Access ProviderGithubAuthData
	rst := initializers.DB.Where("user_id=?", ctx.OwnerUserID).First(&OwnerOAuth2Access)
	if rst.Error != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Internal server error.")
		return
	}

	token := OwnerOAuth2Access.AccessToken

	url := fmt.Sprintf("https://api.github.com/repos/%s/stargazers", target)
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		log.Print(err)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Github API is not reachable")
		return
	}

	req.Header.Set("Accept", "application/vnd.github.star+json")
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("X-GitHub-Api-Version", "2022-11-28")

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		log.Print(err)
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Github API is not reachable")
		return
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			log.Print(err)
		}
	}(resp.Body)

	if resp.StatusCode != http.StatusOK {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, fmt.Sprintf("Github API error: %s", resp.Status))
		return
	}

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to read response body")
		return
	}

	var starDetails []StarDetail
	if err := json.Unmarshal(respBody, &starDetails); err != nil {
		logEngine.NewLogEntry(ctx.WorkflowID, models.ErrorLog, "Failed to parse Github response")
		return
	}

	now := time.Now()
	for _, star := range starDetails {
		starredAt, err := time.Parse(time.RFC3339, star.StarredAt)
		if err != nil {
			logEngine.NewLogEntry(ctx.WorkflowID, models.WarnLog, "Error occurred during date parsing of github response")
			continue
		}

		if now.Sub(starredAt) <= time.Minute {
			workflowEngine.RunWorkflow(ctx)
			return
		}
	}
}

func TriggerNewStarOnRepo(ctx models.Context) error {
	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkNewStarOnRepo, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}
