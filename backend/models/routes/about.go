package routes

type AboutClient struct {
	Host string `json:"host"`
}

type AboutServer struct {
	CurrentTime int64 `json:"current_time"`
}

type AboutServiceDetail struct {
	Name        string `json:"name"`
	Description string `json:"description"`
}

type AboutService struct {
	Name      string               `json:"name"`
	Actions   []AboutServiceDetail `json:"actions"`
	Reactions []AboutServiceDetail `json:"reactions"`
}

type AboutResponse struct {
	Client   AboutClient    `json:"client"`
	Server   AboutServer    `json:"server"`
	Services []AboutService `json:"services"`
}
