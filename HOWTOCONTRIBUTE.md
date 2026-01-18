# How to Contribute

### Local Development Setup

#### Backend Development

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies:
```bash
go mod tidy
```

3. Set up environment variables:
```bash
cp .env.example .env
# Configure database and service credentials
```

4. Run database migrations:
```bash
go run migrate/migrate.go
```

5. Start the development server:
```bash
go run main.go
```

#### Frontend Development

1. Navigate to web directory:
```bash
cd apps/web
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm start
```

#### Mobile Development

1. Open Android Studio
2. Open the `apps/mobile` directory as an Android project
3. Sync Gradle dependencies
4. Run on emulator or connected device

## Configuration

### Environment Variables

Required environment variables for backend:

```bash
# Database Configuration
DB_HOST=localhost
DB_USER=area
DB_PASSWORD=password
DB_NAME=area
DB_PORT=5432

# Server Configuration
PORT=8080
PUBLIC_URL=http://localhost:8080
GIN_MODE=debug

# JWT Configuration
JWT_SECRET=your-secret-key

# Service Integrations
GITHUB_OAUTH2_CLIENT_ID=your-github-client-id
GITHUB_OAUTH2_CLIENT_SECRET=your-github-client-secret
```

### Service Provider Setup

Each service provider requires specific configuration:

#### GitHub Integration

1. Create a GitHub OAuth App:
   - Go to GitHub Settings > Developer settings > OAuth Apps
   - Create new OAuth App
   - Set Authorization callback URL to: `http://localhost:8080/providers/github/auth/callback`

2. Add credentials to environment:
```bash
GITHUB_OAUTH2_CLIENT_ID=your_client_id
GITHUB_OAUTH2_CLIENT_SECRET=your_client_secret
```

## Usage

### Creating a Workflow

1. **Authentication**: Sign up or log in to the platform
2. **Service Authorization**: Authorize the services you want to connect
3. **Trigger Selection**: Choose an action that will trigger the workflow
4. **Reaction Configuration**: Select what should happen when the trigger fires
5. **Parameter Setup**: Configure any required parameters for both trigger and reaction
6. **Activation**: Save and activate the workflow

### Example Workflows

**Timer to GitHub Issue Creation**:
- Trigger: Timer service "Repeat every" action with cron expression
- Reaction: GitHub service "Create an issue" action
- Parameters: Repository name, issue title, issue body

**GitHub Push to Notification**:
- Trigger: GitHub service "New push" action on specific repository
- Reaction: Email service "Send notification" action
- Parameters: Email address, notification template

## API Reference

The backend exposes RESTful APIs for all functionality:

### Authentication Endpoints

- `POST /auth/signup` - Create new user account
- `POST /auth/sign-in` - Authenticate user

### Workflow Management

- `GET /workflows` - List user workflows
- `POST /workflows` - Create new workflow
- `PUT /workflows/{id}` - Update workflow
- `DELETE /workflows/{id}` - Delete workflow

### Service Information

- `GET /about.json` - List available services and their capabilities
- `GET /providers/{service}/auth/init` - Initialize service authorization
- `GET /providers/{service}/auth/callback` - Handle authorization callback

### API Documentation

Complete OpenAPI specification available at `/openapi.json` when server is running.
You can also find a swagger available on port `8082`.

## Supported Services

### Current Integrations

- **Timer**: Schedule-based triggers with cron expressions
- **GitHub**: Repository events and issue management

### Planned Integrations

- Email providers (Gmail, Outlook)
- Social media platforms (Twitter, Facebook)
- Cloud storage (Google Drive, Dropbox)
- Communication tools (Slack, Discord)
- Development tools (Jira, Trello)

## Development

### Adding New Service Providers

See [HOWTOCONTRIBUTE.md](HOWTOCONTRIBUTE.md) for detailed instructions on implementing new service integrations.

### Project Structure

```
area/
├── backend/                 # Go API server
│   ├── controllers/         # HTTP request handlers
│   ├── models/             # Database models
│   ├── services/           # Service provider integrations
│   └── main.go            # Application entry point
├── apps/
│   ├── web/               # React web application
│   └── mobile/            # Android application
├── docker-compose.yml     # Container orchestration
├── ARCHITECTURE.md        # Technical architecture documentation
└── HOWTOCONTRIBUTE.md    # Contribution guidelines
```

### Testing

Run backend tests:
```bash
cd backend
go test ./...
```

Run frontend tests:
```bash
cd apps/web
npm test
```

### Code Quality

The project follows standard Go and JavaScript conventions:

- Go code formatted with `gofmt`
- JavaScript code formatted with Prettier
- ESLint for JavaScript code quality
- GORM for database operations
- Gin framework for HTTP routing

## Deployment

### Production Deployment

1. Configure production environment variables
2. Build Docker images:
```bash
docker-compose build
```

3. Deploy with production compose file:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Environment-Specific Configuration

- **Development**: Debug logging, hot reload, local database
- **Staging**: Production-like environment for testing
- **Production**: Optimized builds, secure configurations, external database

## Security

### Data Protection

- User passwords hashed with secure algorithms
- Service tokens encrypted before storage
- HTTPS enforced in production environments
- Input validation on all API endpoints

### Privacy

- Users control their own data and workflow configurations
- Service authorizations can be revoked at any time
- No data sharing between users without explicit consent
- Audit logs for workflow executions

## Monitoring and Logging

### Application Metrics

- Workflow execution success rates
- API response times and error rates
- Service availability monitoring
- Database performance metrics

### Debugging

- Structured logging for all components
- Error tracking and alerting
- Performance profiling capabilities
- Request tracing for debugging

## Contributing

Contributions are welcome! Please read [HOWTOCONTRIBUTE.md](HOWTOCONTRIBUTE.md) for guidelines on:

- Adding new service providers
- Submitting bug reports
- Proposing new features
- Code style and testing requirements

### Development Workflow

1. Fork the repository
2. Create feature branch from main
3. Implement changes with tests
4. Submit pull request with description of changes

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
