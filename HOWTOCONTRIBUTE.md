# How to Contribute

## Adding a New Third-Party Provider

This guide explains how to implement a new third-party provider for the AREA platform.

### 1. Create Provider Directory

Create a new directory under `backend/services/` with your provider name:
```
backend/services/yourprovider/
├── yourprovider.go     # Main provider definition
├── auth.go            # Authentication handlers (optionnal)
├── handlers.go        # Reaction handlers (optionnal)
├── db.go             # Database models (optionnal)
└── triggers.go       # Trigger functions (optionnal)
```

### 2. Define Your Provider Service

Create the main provider file (`yourprovider.go`):

```go
package yourprovider

import "dawpitech/area/models"

var Provider = models.Service{
    Name: "YourProvider",
    Icon: "icon-url-or-path",
    Actions: []models.Action{
        // Define your triggers here
    },
    Reactions: []models.Reaction{
        // Define your reactions here
    },
    AuthMethod: &models.Authentification{
        // Only if authentication is needed
        HandlerAuthInit:     AuthInit,
        HandlerAuthCallback: AuthCallback,
        RouteAuthInit:       "/providers/yourprovider/auth/init",
        RouteAuthCallback:   "/providers/yourprovider/auth/callback",
    },
    DBModels: []interface{}{
        // Database models for storing provider data
    },
}
```

### 3. Implement Actions

Actions are events that users wait for. Define them in your provider:

```go
Actions: []models.Action{
    {
        Name:        "New Message",
        Description: "Triggers when a new message is received",
        Parameters: []string{
            "channel_id",
            "keyword", // optional
        },
        Trigger: TriggerNewMessage,
    },
}
```

Implement the trigger function in `triggers.go`:

```go
func TriggerNewMessage(ctx models.TriggerContext) error {
    channelID := ctx.ActionParameters[0]
    keyword := ""
    if len(ctx.ActionParameters) > 1 {
        keyword = ctx.ActionParameters[1]
    }
    
    // Set up monitoring for new messages
    // This should register some kind of listener/webhook/polling
    // When the event occurs, call the workflow engine to execute reactions
    
    return nil
}
```

### 4. Implement Reactions

Reactions are things that happen when an action is fulfilled.

```go
Reactions: []models.Reaction{
    {
        Name:        "Send Message",
        Description: "Send a message to a channel",
        Parameters: []string{
            "channel_id",
            "message",
        },
        Handler: HandlerSendMessage,
    },
}
```

Implement the handler in `handlers.go`:

```go
func HandlerSendMessage(ctx models.HandlerContext) error {
    channelID := ctx.ReactionParameters[0]
    message := ctx.ReactionParameters[1]
    
    // Get user's auth data
    var authData YourProviderAuthData
    err := initializers.DB.Where("user_id = ?", ctx.OwnerUserID).First(&authData).Error
    if err != nil {
        return err
    }
    
    // Use auth data to call provider API
    // Send the message using your provider's API
    
    return nil
}
```

### 5. Add Authentication (If Required)

If your provider requires OAuth or API keys, implement authentication in `auth.go`:

```go
func AuthInit(c *gin.Context) (*AuthInitResponse, error) {
    // Generate OAuth URL or return instructions for API key setup
    return &AuthInitResponse{
        AuthURL: "https://yourprovider.com/oauth/authorize?...",
    }, nil
}

func AuthCallback(c *gin.Context) (*AuthCallbackResponse, error) {
    // Handle OAuth callback or API key validation
    // Store auth data in database
    return &AuthCallbackResponse{
        Success: true,
    }, nil
}
```

### 6. Define Database Models (If Needed)

Create models for storing user authentication data in `db.go`:

```go
type YourProviderAuthData struct {
    gorm.Model
    UserID      uint   `gorm:"uniqueIndex"`
    AccessToken string
    RefreshToken string
    ExpiresAt   time.Time
}
```

### 7. Register Your Provider

Add your provider to the services list in `backend/services/manager.go`:

```go
var Services = []models.Service{
    github.Provider,
    yourprovider.Provider, // Add this line
}
```

### 8. Key Guidelines

**Authentication:**
- Only implement `AuthMethod` if your provider requires user authentication
- Services like Timer don't need authentication (set `AuthMethod: nil`)

**Parameters:**
- Keep parameter names simple and descriptive
- Document what each parameter does in the Description field
- Parameters are passed as string arrays

**Error Handling:**
- Always return meaningful errors
- Check for missing authentication data
- Validate parameters before using them

**Testing:**
- Test your handlers manually first
- Ensure triggers properly register and fire
- Verify authentication flow works end-to-end

### 9. Common Patterns

**Polling-based triggers:**
```go
// Set up a goroutine that polls the API periodically
go func() {
    ticker := time.NewTicker(30 * time.Second)
    for range ticker.C {
        // Check for new events
        // If found, notify workflow engine
    }
}()
```

**Webhook-based triggers:**
```go
// Register a webhook with the provider
// When webhook is called, trigger the workflow
```

**API rate limiting:**
```go
// Implement proper rate limiting for API calls
// Store last request time to avoid hitting limits
```

### 10. Examples

Look at existing providers for reference:
- `backend/services/github/` - OAuth-based third-party provider
- `backend/services/timer/` - Simple internal provider without auth

### Need Help?

If you get stuck:
1. Check existing provider implementations
2. Ensure your provider is properly registered in manager.go
3. Verify authentication flow if using OAuth
4. Test individual components (auth, handlers, triggers) separately
