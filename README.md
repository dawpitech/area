# AREA

An automation platform that connects different services through customizable workflows, similar to IFTTT or Zapier.

## Overview

AREA allows users to create automated workflows by connecting triggers from one service to actions in another service. For example, automatically create a GitHub issue every time a timer expires, or send a notification when new content is posted.

## Features

- **Multi-platform Support**: Web application, Android mobile app
- **Service Integration**: Connect popular third-party services through standardized APIs
- **Custom Workflows**: Create personalized automation rules with configurable parameters
- **Real-time Execution**: Workflows execute immediately when trigger conditions are met
- **User Authentication**: Secure OAuth integration with supported services
- **Extensible Architecture**: Easy integration of new service providers

## Architecture

The platform consists of three main components:

- **Backend**: Go-based API server handling workflow management and service integrations
- **Web Frontend**: React application for workflow creation and management
- **Mobile App**: Android application for on-the-go workflow monitoring

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed technical documentation.

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Go 1.19+ (for local development)
- Node.js 16+ (for frontend development)
- Android Studio (for mobile development)

### Quick Start with Docker

1. Clone the repository:
```bash
git clone https://github.com/dawpitech/area.git
cd area
```

2. Configure environment variables:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Start the services:
```bash
docker-compose up -d
```

4. Access the application:
- Web interface: http://localhost:3000
- API documentation: http://localhost:8080/openapi.json

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

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) file for details.

## Support

### Documentation

- [Architecture Guide](ARCHITECTURE.md) - Technical system overview
- [Contribution Guide](HOWTOCONTRIBUTE.md) - Developer instructions
- API Documentation - Available at `/openapi.json` endpoint

### Community

- Issues: Report bugs or request features via GitHub Issues
- Discussions: Technical questions and community discussions
- Pull Requests: Code contributions and improvements

## Roadmap

### Short Term

- Complete core service integrations
- Mobile application feature parity
- Workflow scheduling and history
- Enhanced error handling and logging

### Long Term

- Workflow templates and sharing
- Advanced conditional logic
- Real-time workflow monitoring
- Enterprise authentication integration
- Multi-tenant deployment support
