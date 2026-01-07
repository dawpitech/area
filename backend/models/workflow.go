package models

import "gorm.io/gorm"

type Workflow struct {
	gorm.Model
	OwnerUserID        uint
	ActionName         string
	ActionParameters   map[string]string `gorm:"serializer:json"`
	ReactionName       string
	ReactionParameters map[string]string `gorm:"serializer:json"`
	Active             bool
}
