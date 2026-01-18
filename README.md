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
- Web interface: http://localhost:8081
- API documentation: http://localhost:8082
- API endpoint: http://localhost:8080

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
