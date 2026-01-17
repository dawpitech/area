package notion

import (
	"encoding/json"
	"io"
	"log"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
)

var (
	mu     sync.Mutex
	events []any
)

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

func TriggerNotionPageCreated(g gin.Context) error {
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
	HandleNotionEvent(event)

	g.Status(200)
	return nil
}

func HandleNotionEvent(event WebhookEvent) {
	switch event.Type {

	case "page.created":
		OnPageCreated(event)

	case "page.deleted":
		OnPageDeleted(event)

	case "page.undeleted":
		OnPageUndeleted(event)

	default:
		log.Printf("Unhandled Notion event: %s", event.Type)
	}
}

func OnPageCreated(event WebhookEvent) {
	log.Printf(
		"[Notion] Page created: page_id=%s at %s\n",
		event.Entity.ID,
		event.Timestamp,
	)
}

func OnPageDeleted(event WebhookEvent) {
	log.Printf(
		"[Notion] Page deleted: page_id=%s\n",
		event.Entity.ID,
	)
}

func OnPageUndeleted(event WebhookEvent) {
	log.Printf(
		"[Notion] Page restored: page_id=%s\n",
		event.Entity.ID,
	)
}
