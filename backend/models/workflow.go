package models

import "gorm.io/gorm"

type Workflow struct {
	gorm.Model
	OwnerUserID        uint
	ActionName         string
	ActionParameters   []string `gorm:"serializer:json"`
	ReactionName       string
	ReactionParameters []string `gorm:"serializer:json"`
}
