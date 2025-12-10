package routes

type AuthRequest struct {
	Email    string `json:"email" validate:"required"`
	Password string `json:"password" validate:"required"`
}

type AuthResponse struct {
	Token string `json:"token"`
}

type ThirdPartyAuthCheck struct {
	IsConnected bool `json:"is_connected"`
}
