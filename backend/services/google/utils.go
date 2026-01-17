package google

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

func getGmailAddress(client *http.Client) (string, error) {
	url := "https://gmail.googleapis.com/gmail/v1/users/me/profile"

	// Create a new HTTP request
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to make request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Read and parse the response body
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read response body: %w", err)
	}

	var profile GmailProfile
	if err := json.Unmarshal(body, &profile); err != nil {
		return "", fmt.Errorf("failed to parse JSON response: %w", err)
	}

	return profile.EmailAddress, nil
}
