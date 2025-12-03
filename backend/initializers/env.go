package initializers

import (
	"github.com/joho/godotenv"
)

func LoadEnvironment() {
	_ = godotenv.Load()
}
