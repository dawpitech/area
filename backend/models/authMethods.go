package models

import "gorm.io/gorm"

type AuthMethods struct {
	gorm.Model
	UserID       uint
	Email        string `json:"email" gorm:"unique"`
	PasswordHash string `json:"password_hash"`
}
