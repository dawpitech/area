package routes

type UserCreationRequest struct {
	Email    string `json:"email" validate:"required"`
	Password string `json:"password" validate:"required"`
}

type UserCreationResponse struct {
	Status string `json:"status"`
}
