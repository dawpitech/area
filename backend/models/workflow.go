package models

import "gorm.io/gorm"

type Workflow struct {
	gorm.Model
	Name               string
	OwnerUserID        uint
	ActionName         string
	ActionParameters   map[string]string `gorm:"serializer:json"`
	ModifierName       string
	ModifierParameters map[string]string `gorm:"serializer:json"`
	ReactionName       string
	ReactionParameters map[string]string `gorm:"serializer:json"`
	Active             bool
}
