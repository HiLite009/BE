# Project Overview

This is a Spring Boot application named `hilite`.

## Technologies Used

*   **Spring Boot**: 3.4.4
*   **Java**: 21
*   **Gradle**: For build automation
*   **Spring Data JPA**: For data persistence
*   **Spring Security**: For authentication and authorization
*   **JJWT**: For JSON Web Token handling
*   **MySQL Connector/J**: For MySQL database connectivity
*   **H2 Database**: For in-memory database in CI environment
*   **Lombok**: To reduce boilerplate code
*   **Spring Boot DevTools**: For development time utilities
*   **Springdoc OpenAPI**: For API documentation (Swagger UI)
*   **Spring Boot Starter Validation**: For data validation
*   **Checkstyle**: For code style checking
*   **Spotless**: For code formatting

## Project Structure

The main application entry point is `org.example.hilite.HiliteApplication`.

The project follows a standard Spring Boot application structure:

*   `src/main/java`: Contains the main Java source code.
    *   `org.example.hilite`: Base package for the application.
        *   `common`: Common utilities, exceptions, and base classes.
        *   `config`: Spring configuration classes (Security, Swagger, Data Initializer).
        *   `controller`: REST API controllers.
        *   `dto`: Data Transfer Objects for requests and responses.
        *   `entity`: JPA entities representing database tables.
        *   `filter`: Servlet filters (e.g., JWT filter).
        *   `repository`: Spring Data JPA repositories for data access.
        *   `service`: Business logic services.
*   `src/main/resources`: Contains application resources.
    *   `application.yml`: Main application configuration.
    *   `application-test.yml`: Configuration for testing environment.
    *   `logback-spring.xml`: Logging configuration.
*   `src/test/java`: Contains test source code.

## Configuration

The application uses `application.yml` for configuration.
The active profile is set to `private`.
JPA `open-in-view` is set to `false`.
Logging levels are configured for `root`, `org.example`, and `org.springframework.security`.

## How to Run

This is a Spring Boot application. You can run it using Gradle:

```bash
./gradlew bootRun
```

## Build and Test

To build the project:

```bash
./gradlew build
```

To run tests:

```bash
./gradlew test
```

To apply code formatting and check style:

```bash
./gradlew spotlessApply
./gradlew checkstyleMain
```