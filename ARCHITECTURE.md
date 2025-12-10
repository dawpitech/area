# AREA Platform Architecture

## Overview

AREA is an automation platform that enables users to create workflows connecting different services through triggers and reactions. The system follows a microservices architecture with a Go backend, React web frontend, and Android mobile application.

## System Components

### Backend (Go)

The backend is built using the Go programming language and follows a modular architecture.

#### Core Structure

```
backend/
├── main.go                 # Application entry point
├── controllers/            # HTTP request handlers
├── models/                 # Data models and database schemas
├── services/              # Third-party service integrations
├── middlewares/           # HTTP middleware functions
├── initializers/          # Database and environment setup
├── migrate/               # Database migration utilities
└── utils/                 # Shared utility functions
```

#### Framework and Libraries

- **Gin**: HTTP web framework for routing and middleware
- **Fizz**: API documentation and OpenAPI spec generation
- **GORM**: Object-relational mapping for database operations
- **OAuth2**: Authentication with third-party services

#### Database Layer

The system uses GORM for database abstraction. Core models include:

- `User`: User account information
- `Workflow`: Automation configurations linking actions to reactions
- `Service`: Third-party provider definitions
- Provider-specific models for storing authentication data

#### Service Architecture

Services are modular components that integrate third-party APIs. Each service defines:

- **Actions**: Events that can trigger workflows (e.g., "new email received")
- **Reactions**: Operations that can be executed (e.g., "send notification")
- **Authentication**: OAuth or API key handling for user authorization
- **Database Models**: Provider-specific data storage requirements

#### API Design

The backend exposes RESTful APIs for:

- User authentication and management
- Workflow creation and management
- Service authorization and configuration
- System metadata and service discovery

### Frontend Applications

#### Web Application (React)

```
apps/web/
├── public/                # Static assets
├── src/
│   ├── components/        # React components
│   ├── contexts/          # React context providers
│   ├── api/              # API client functions
│   └── index.js          # Application entry point
├── package.json          # Dependencies and scripts
└── tailwind.config.js    # CSS framework configuration
```

**Technology Stack:**
- React for component-based UI
- Tailwind CSS for styling
- Context API for state management
- Fetch API for backend communication

#### Mobile Application (Android/Kotlin)

```
apps/mobile/
├── app/src/main/
│   ├── AndroidManifest.xml    # App configuration
│   ├── java/com/uwu/area/     # Kotlin source code
│   └── res/                   # Android resources
├── build.gradle.kts           # Build configuration
└── gradle/                    # Gradle wrapper and dependencies
```

**Technology Stack:**
- Kotlin for Android development
- Gradle for build management
- Android SDK for platform integration

## Data Flow

### Workflow Creation

1. User authenticates with AREA platform
2. User selects a trigger service and configures parameters
3. User selects a reaction service and configures parameters
4. System stores workflow configuration in database
5. System registers trigger monitoring with appropriate service
6. System validates user has necessary permissions for both services

### Workflow Execution

1. External service triggers an event
2. Service module detects the event
3. System queries database for matching workflows
4. System retrieves user authentication data for reaction service
5. System executes the configured reaction
6. System logs execution result

## Service Integration Pattern

### Provider Structure

Each service provider implements a standardized interface:

```go
type Service struct {
    Name       string          // Human-readable service name
    Icon       string          // Service icon or logo reference
    Actions    []Action        // Available triggers
    Reactions  []Reaction      # Available operations
    AuthMethod *Authentication # OAuth/API key configuration
    DBModels   []interface{}   # Database models for service data
}
```

### Action Definition

Actions represent events that can trigger workflows:

```go
type Action struct {
    Name        string       # User-facing action name
    Description string       # Action behavior description
    Parameters  []string     # Required configuration parameters
    Trigger     TriggerSetup # Function to initialize event monitoring
}
```

### Reaction Definition

Reactions represent operations that can be executed:

```go
type Reaction struct {
    Name        string          # User-facing reaction name
    Description string          # Reaction behavior description
    Parameters  []string        # Required configuration parameters
    Handler     HandlerCallback # Function to execute the operation
}
```

## Authentication and Authorization

### User Authentication

The platform uses JWT tokens for user session management. Users authenticate through:

- Email/password registration and login
- Session tokens for API access
- Middleware validation for protected endpoints

### Service Authorization

Third-party service access uses OAuth 2.0 where supported:

- Authorization flow redirects users to service provider
- System stores access and refresh tokens
- Tokens are used for API calls on behalf of users
- Token refresh is handled automatically when possible

## Deployment

### Container Configuration

The system uses Docker for containerization:

```yaml
# docker-compose.yml structure
services:
  backend:    # Go application server
  database:   # PostgreSQL or similar database
  frontend:   # React application (if served from containers)
```

### Environment Configuration

Configuration is managed through environment variables:

- Database connection parameters
- OAuth client credentials for each service
- API endpoints and service URLs
- Security keys and tokens

## Scalability Considerations

### Horizontal Scaling

- Backend services are stateless and can be replicated
- Database connections are pooled for efficiency
- Session data is stored in database, not server memory

### Performance Optimization

- Database queries use indexes on frequently accessed fields
- API responses include only necessary data
- Long-running operations are handled asynchronously where possible

## Security

### Data Protection

- User passwords are hashed using secure algorithms
- Service tokens are encrypted before database storage
- API communications use HTTPS in production
- Input validation prevents injection attacks

### Access Control

- Users can only access their own workflows and data
- Service permissions are validated before workflow creation
- API endpoints require authentication for sensitive operations

## Monitoring and Logging

### Error Handling

- Service errors are logged with sufficient detail for debugging
- User-facing errors provide helpful but secure messages
- System errors are captured for operational monitoring

### Operational Metrics

- API response times and error rates
- Workflow execution success and failure rates
- Service availability and response times
- Database performance and query analysis

## Development Guidelines

### Code Organization

- Services are implemented as separate packages
- Database models are centralized in the models package
- API controllers handle request/response formatting only
- Business logic is implemented in service-specific modules

### Testing Strategy

- Unit tests for individual service functions
- Integration tests for complete workflow execution
- API endpoint testing for request/response validation
- Authentication flow testing for each supported service

### Documentation Requirements

- API endpoints documented with OpenAPI specifications
- Service integration documented with example configurations
- Database schema changes documented with migration scripts
- Deployment procedures documented with environment setup