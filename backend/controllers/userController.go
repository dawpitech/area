package controllers

import (
	"dawpitech/area/crypto"
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"github.com/gin-gonic/gin"
	"github.com/juju/errors"
	"log"
)

func GetAllUsers(_ *gin.Context) (*[]models.User, error) {
	var users []models.User
	if rst := initializers.DB.Find(&users); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}
	return &users, nil
}

func CreateNewUser(_ *gin.Context, in *routes.UserCreationRequest) (*routes.UserCreationResponse, error) {
	var count int64
	if rst := initializers.DB.
		Model(&models.AuthMethods{}).
		Where("email=?", in.Email).
		Count(&count); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}

	if count > 0 {
		return nil, errors.NewAlreadyExists(nil, "An user already exist with this email address ")
	}

	hash, err := crypto.GenerateEncodedHash(in.Password)
	if err != nil {
		log.Print(err.Error())
		return nil, errors.New("Internal server error.")
	}

	user := models.User{
		Username: "placeholder",
		Auth: models.AuthMethods{
			Email:        in.Email,
			PasswordHash: hash,
		},
	}

	if rst := initializers.DB.Create(&user); rst.Error != nil {
		return nil, errors.New("Internal server error")
	}

	return &routes.UserCreationResponse{
		Status: "ok",
	}, nil
}
