package controllers

import (
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/models/routes"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
	"github.com/juju/errors"
	"gorm.io/gorm"
	"os"
	"time"
)

func LoginUser(_ *gin.Context, in *routes.AuthRequest) (*routes.AuthResponse, error) {
	var userFound models.User
	rst := initializers.DB.Joins("Auth").Where("email=?", in.Email).First(&userFound)
	if rst.Error != nil {
		if errors.Is(rst.Error, gorm.ErrRecordNotFound) {
			return nil, errors.NewUserNotFound(nil, "User not found.")
		}
		return nil, errors.New("Internal server error.")
	}

	if in.Password != userFound.Auth.PasswordHash {
		return nil, errors.NewForbidden(nil, "Invalid password.")
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"user_id": userFound.ID,
		"exp":     time.Now().Add(time.Hour * 24).Unix(),
	})

	signedToken, err := token.SignedString([]byte(os.Getenv("JWT_TOKENS_SECRET")))

	if err != nil {
		return nil, errors.New("Internal server error.")
	}

	return &routes.AuthResponse{
		Token: signedToken,
	}, nil
}
