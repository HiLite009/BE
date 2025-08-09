# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.x application called "hilite-server" that provides a web API with authentication, role-based access control, and chatbot functionality. It uses Java 21, Gradle, MySQL/H2, Spring Security with JWT tokens, and includes comprehensive API documentation with OpenAPI/Swagger.

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Generate test reports and API docs
./gradlew asciidoctor
```

### Code Quality
```bash
# Check code style with Checkstyle (Google Java Format)
./gradlew checkstyleMain checkstyleTest

# Apply code formatting
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with output directory for REST Docs
./gradlew test --info
```

## Architecture Overview

### Package Structure
- `config/` - Spring configuration classes (Security, Swagger, JPA)
- `controller/` - REST API endpoints organized by domain
- `service/` - Business logic layer
- `repository/` - JPA data access layer  
- `entity/` - JPA entities with audit fields
- `dto/` - Request/Response DTOs organized by request/response
- `common/` - Shared utilities, exceptions, and base classes
- `filter/` - Custom security filters (JWT)

### Key Components

**Security Architecture:**
- JWT-based authentication with `JwtFilter` and `JwtUtil`
- Dynamic role-based authorization via `DynamicAuthorizationManager`
- Custom `UserDetails` implementation
- Global exception handling

**Domain Model:**
- `Member` - User accounts with roles
- `Role` - User roles (ADMIN, USER, etc.)  
- `AccessPage` - Protected resources/pages
- `RolePagePermission` - Permission mappings between roles and pages

**API Structure:**
- Authentication endpoints (`/api/auth/*`)
- Admin endpoints (`/api/admin/*`) 
- Member management (`/api/members/*`)
- ChatBot endpoints (basic implementation)
- Permission management

### Database
- Production: MySQL
- Testing: H2 in-memory database
- JPA with Hibernate, audit fields via `@EnableJpaAuditing`
- Base entity class with created/modified timestamps

### Documentation
- REST Docs integration with Spring REST Docs
- OpenAPI 3.0 specification generation
- Swagger UI available when running

## Configuration Files
- `application.yml` - Base configuration
- `application-private.yml` - Private/production settings
- `application-test.yml` - Test environment settings
- `config/google-check.xml` - Checkstyle configuration

## Code Style
The project uses Google Java Format with Checkstyle enforcement. Always run `./gradlew spotlessApply` before committing changes.