package notion

import (
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/models"
	"encoding/json"
	"io"
	"log"
	"sync"
	"time"

	"github.com/go-co-op/gocron/v2"
	"github.com/google/uuid"
	"github.com/juju/errors"

	"github.com/gin-gonic/gin"
)

var (
	mu              sync.Mutex
	events          []any
	scheduler       gocron.Scheduler
	workflowJobUUID = make(map[uint]uuid.UUID)
)

func init() {
	var err error
	if scheduler, err = gocron.NewScheduler(); err != nil {
		log.Panic("Module notion couldn't init a job scheduler")
	}
	scheduler.Start()
}

func StoreEvent(event any) {
	mu.Lock()
	defer mu.Unlock()
	events = append(events, event)
}

func GetEvents() []any {
	mu.Lock()
	defer mu.Unlock()
	return append([]any{}, events...)
}

func ClearEventsByType(eventType string) {
	mu.Lock()
	defer mu.Unlock()
	var filteredEvents []any
	for _, eventInterface := range events {
		event, ok := eventInterface.(WebhookEvent)
		if !ok {
			filteredEvents = append(filteredEvents, eventInterface)
			continue
		}
		if event.Type != eventType {
			filteredEvents = append(filteredEvents, eventInterface)
		}
	}
	events = filteredEvents
}

type WebhookEvent struct {
	ID             string    `json:"id"`
	Type           string    `json:"type"`
	Timestamp      time.Time `json:"timestamp"`
	WorkspaceID    string    `json:"workspace_id"`
	SubscriptionID string    `json:"subscription_id"`
	IntegrationID  string    `json:"integration_id"`
	AttemptNumber  int       `json:"attempt_number"`

	Entity struct {
		ID   string `json:"id"`
		Type string `json:"type"`
	} `json:"entity"`

	Data map[string]any `json:"data"`
}

func TriggerNotion(g *gin.Context) error {
	body, err := io.ReadAll(g.Request.Body)
	if err != nil {
		return err
	}

	var verification struct {
		VerificationToken string `json:"verification_token"`
	}
	if err := json.Unmarshal(body, &verification); err == nil && verification.VerificationToken != "" {
		log.Println("notion verification token:", verification.VerificationToken)

		g.Status(200)
		return nil
	}

	var event WebhookEvent
	if err := json.Unmarshal(body, &event); err != nil {
		return err
	}

	StoreEvent(event)

	g.Status(200)
	return nil
}

func EmptyRemove(ctx models.Context) error {
	return nil
}

func checkNotionPageCreated(ctx models.Context) {
	events := GetEvents()

	for _, eventInterface := range events {
		event, ok := eventInterface.(WebhookEvent)
		if !ok {
			continue
		}

		if event.Type == "page.created" {
			ctx.RuntimeData = make(map[string]string)
			ctx.RuntimeData["page_id"] = event.Entity.ID
			ctx.RuntimeData["timestamp"] = event.Timestamp.Format(time.RFC3339)
			ctx.RuntimeData["workspace_id"] = event.WorkspaceID

			ClearEventsByType("page.created")
			workflowEngine.RunWorkflow(ctx)
			return
		}
	}
}

func checkNotionPageDeleted(ctx models.Context) {
	events := GetEvents()

	for _, eventInterface := range events {
		event, ok := eventInterface.(WebhookEvent)
		if !ok {
			continue
		}

		if event.Type == "page.deleted" {
			ctx.RuntimeData = make(map[string]string)
			ctx.RuntimeData["page_id"] = event.Entity.ID
			ctx.RuntimeData["timestamp"] = event.Timestamp.Format(time.RFC3339)
			ctx.RuntimeData["workspace_id"] = event.WorkspaceID

			ClearEventsByType("page.deleted")
			workflowEngine.RunWorkflow(ctx)
			return
		}
	}
}

func checkNotionPageRestored(ctx models.Context) {
	events := GetEvents()

	for _, eventInterface := range events {
		event, ok := eventInterface.(WebhookEvent)
		if !ok {
			continue
		}

		if event.Type == "page.undeleted" {
			ctx.RuntimeData = make(map[string]string)
			ctx.RuntimeData["page_id"] = event.Entity.ID
			ctx.RuntimeData["timestamp"] = event.Timestamp.Format(time.RFC3339)
			ctx.RuntimeData["workspace_id"] = event.WorkspaceID

			ClearEventsByType("page.undeleted")
			workflowEngine.RunWorkflow(ctx)
			return
		}
	}
}

func SetupNotionPageCreatedTrigger(ctx models.Context) error {
	ClearEventsByType("page.created")

	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkNotionPageCreated, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}

func RemoveNotionPageCreatedTrigger(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func SetupNotionPageDeletedTrigger(ctx models.Context) error {
	ClearEventsByType("page.deleted")

	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkNotionPageDeleted, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}

func RemoveNotionPageDeletedTrigger(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}

func SetupNotionPageRestoredTrigger(ctx models.Context) error {
	ClearEventsByType("page.undeleted")

	job, err := scheduler.NewJob(
		gocron.CronJob("* * * * *", false),
		gocron.NewTask(checkNotionPageRestored, ctx),
	)

	if err != nil {
		return errors.New("Set-up of the trigger failed, please re-try later. Err: " + err.Error())
	}
	workflowJobUUID[ctx.WorkflowID] = job.ID()

	return nil
}

func RemoveNotionPageRestoredTrigger(ctx models.Context) error {
	err := scheduler.RemoveJob(workflowJobUUID[ctx.WorkflowID])
	if err != nil {
		return errors.New("Removal of given job resulted in an error. Err " + err.Error())
	}
	delete(workflowJobUUID, ctx.WorkflowID)
	return nil
}
