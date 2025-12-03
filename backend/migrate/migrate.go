package main

import (
	"dawpitech/area/initializers"
	"dawpitech/area/models"
	"dawpitech/area/services"
	"log"
)

func init() {
	initializers.LoadEnvironment()
	initializers.ConnectDB()
}

func main() {
	log.Println("Starting migration.")
	log.Println("Migrating system tables.")

	err := initializers.DB.AutoMigrate(
		&models.User{},
		&models.AuthMethods{},
	)

	if err != nil {
		log.Panic(err.Error())
	}

	for i := 0; i < len(services.Services); i++ {
		log.Printf("Migrating service '%s' tables.\n", services.Services[i].Name)
		err = initializers.DB.AutoMigrate(services.Services[i].DBModels...)

		if err != nil {
			log.Panic(err.Error())
		}
	}

	log.Print("Migration successful.")
}
