package initializers

import (
	"github.com/joho/godotenv"
	"log"
)

func LoadEnvironment() {
	err := godotenv.Load()

	if err != nil {
		log.Fatal("No .env file found")
	}
}
