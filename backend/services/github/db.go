package github

import (
	"gorm.io/gorm"
	"time"
)

type ProviderGithubAuthData struct {
	gorm.Model
	UserID       uint      `gorm:"not null;index"`
	AccessToken  string    `gorm:"size:2048"`
	RefreshToken string    `gorm:"size:2048"`
	TokenExpiry  time.Time `gorm:"index"`
	Scope        string
}
