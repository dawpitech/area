package main

import (
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"log"
)

func init() {
	initializers.LoadEnvironment()
	initializers.ConnectDB()
}

func main() {
	log.Println("Starting migration.")
	err := initializers.DB.AutoMigrate(
		&models.User{},
		&models.AuthMethods{},
	)

	if err != nil {
		log.Panic(err.Error())
	}

	log.Print("Migration successful.")
}
