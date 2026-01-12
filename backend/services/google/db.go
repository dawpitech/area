package google

import "gorm.io/gorm"

type ProviderGoogleAuthData struct {
	gorm.Model
	UserID      uint   `gorm:"not null;index"`
	AccessToken string `gorm:"size:2048"`
	Scope       string
}
