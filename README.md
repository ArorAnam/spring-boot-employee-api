# Employee Management API

A Spring Boot application that provides RESTful APIs for managing employee data. This project implements a proxy API that interacts with a mock employee server and provides additional business logic and data manipulation capabilities.

## Table of Contents
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [API Documentation](#api-documentation)
- [Health Checks & Monitoring](#health-checks--monitoring)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Implementation Details](#implementation-details)
- [Configuration](#configuration)

## Architecture

The project consists of two main modules:
- **Server Module**: Mock employee API server running on port 8112
- **API Module**: The main API implementation running on port 8111

```
Client → API (8111) → Mock Server (8112)
```

## Prerequisites

- Java 17
- Gradle 7.6.4
- Postman (optional, for API testing)

## Getting Started

### 1. Set Java Version
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### 2. Start the Mock Server
```bash
./gradlew server:bootRun
```
The mock server will start on `http://localhost:8112`

### 3. Start the API Server
In a new terminal:
```bash
./gradlew api:bootRun
```
The API will start on `http://localhost:8111`

### 4. Run Tests
```bash
./gradlew api:test
```

### 5. Code Formatting
```bash
./gradlew spotlessApply
```

## API Endpoints

### 1. Get All Employees
```http
GET /api/v1/employee
```
Returns a list of all employees.

### 2. Search Employees by Name
```http
GET /api/v1/employee/search/{searchString}
```
Returns employees whose names contain the search string (case-insensitive).

### 3. Get Employee by ID
```http
GET /api/v1/employee/{id}
```
Returns a single employee by their UUID.

### 4. Get Highest Salary
```http
GET /api/v1/employee/highestSalary
```
Returns the highest salary among all employees as an integer.

### 5. Get Top 10 Highest Earning Employee Names
```http
GET /api/v1/employee/topTenHighestEarningEmployeeNames
```
Returns a list of names of the top 10 highest-paid employees.

### 6. Create Employee
```http
POST /api/v1/employee
Content-Type: application/json

{
  "name": "John Smith",
  "salary": 85000,
  "age": 28,
  "title": "Senior Developer"
}
```
Creates a new employee with validation:
- Name: Required, not blank
- Salary: Required, positive integer
- Age: Required, between 16 and 75
- Title: Required, not blank

### 7. Delete Employee by ID
```http
DELETE /api/v1/employee/{id}
```
Deletes an employee by their UUID and returns the employee's name.

## API Documentation

### OpenAPI/Swagger Documentation
The API is fully documented using OpenAPI 3.0 specifications with Swagger UI.

- **Swagger UI**: `http://localhost:8111/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8111/api-docs`

The documentation includes:
- Complete endpoint descriptions with examples
- Request/response schemas
- Error response formats
- Interactive "Try it out" functionality

### Error Handling
The API implements comprehensive error handling with standardized error responses:

```json
{
  "error": "EMPLOYEE_NOT_FOUND",
  "message": "Employee not found with ID: 123",
  "status": 404,
  "path": "/api/v1/employee/123",
  "timestamp": "2024-01-15 10:30:45",
  "traceId": "abc12345"
}
```

**Error Types:**
- `EMPLOYEE_NOT_FOUND` (404) - Employee does not exist
- `VALIDATION_FAILED` (400) - Input validation errors
- `EXTERNAL_SERVICE_ERROR` (502/503) - Mock server issues
- `INVALID_JSON` (400) - Malformed request body

## Health Checks & Monitoring

### Health Endpoints
- **Application Health**: `http://localhost:8111/actuator/health`
- **Detailed Health**: Shows status of all components including Mock Employee API connectivity and Circuit Breaker status

### Metrics & Monitoring
- **Metrics**: `http://localhost:8111/actuator/metrics`
- **Prometheus**: `http://localhost:8111/actuator/prometheus`

**Custom Metrics:**
- `employees.count` - Current number of employees
- `employees.fetch.success` - Successful fetch operations
- `employees.fetch.error` - Failed fetch operations
- `employees.fetch.duration` - Time taken for fetch operations
- `employees.created` - Number of employees created
- `employees.create.error` - Failed creation attempts

**Resilience4j Metrics:**
- `resilience4j.circuitbreaker.calls` - Circuit breaker call metrics
- `resilience4j.circuitbreaker.state` - Circuit breaker state (CLOSED, OPEN, HALF_OPEN)
- `resilience4j.ratelimiter.calls` - Rate limiter call metrics
- `resilience4j.retry.calls` - Retry attempt metrics
- `resilience4j.circuitbreaker.failure.rate` - Circuit breaker failure rate

### Application Info
- **Info Endpoint**: `http://localhost:8111/actuator/info`
- Shows application name, version, Java version, and description

## Testing

### Unit Tests
The project includes comprehensive unit tests for:
- Controller layer (`EmployeeControllerTest`)
- Service layer (`EmployeeServiceTest`)

### Integration Tests
Full integration tests with MockMvc:
- `EmployeeControllerIntegrationTest` - Tests complete request/response cycles
- Error scenario testing
- Validation testing
- HTTP status code verification

Run tests with:
```bash
./gradlew api:test
```

### Integration Testing with Postman
A Postman collection is provided for easy API testing:
1. Import `employee-api-postman.json` into Postman
2. Set the `employeeId` variable after getting an employee list
3. Run individual requests or the entire collection

### Manual Testing with cURL
```bash
# Get all employees
curl http://localhost:8111/api/v1/employee

# Search by name
curl http://localhost:8111/api/v1/employee/search/John

# Create employee
curl -X POST http://localhost:8111/api/v1/employee \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","salary":50000,"age":25,"title":"Developer"}'
```

## Project Structure

```
java-employee-challenge/
├── api/                          # Main API module
│   ├── src/main/java/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── model/               # DTOs and entities
│   │   └── config/              # Configuration classes
│   └── src/test/java/           # Unit tests
├── server/                       # Mock server module
├── buildSrc/                     # Gradle build configuration
├── employee-api-postman.json     # Postman collection
└── README.md                     # This file
```

## Implementation Details

### Key Features

1. **Resilience Patterns**: 
   - **Circuit Breaker**: Prevents cascade failures by opening circuit when failure threshold is reached
   - **Rate Limiter**: Controls outbound request rate (10 requests/second) to prevent overwhelming the server
   - **Retry with Exponential Backoff**: Automatic retry on transient failures with increasing delays
   - **Response Caching**: Caches successful responses to reduce server load and improve performance
   - **Timeout Management**: Configurable connection and read timeouts
   - **Connection Pooling**: Apache HttpClient with connection pooling for efficient HTTP connections
2. **Advanced Error Handling**: 
   - Custom exception classes with specific error types
   - Global exception handler with @ControllerAdvice
   - Standardized error response format with trace IDs
   - Graceful degradation with fallback methods
3. **Input Validation**: 
   - Jakarta Bean Validation with custom constraints
   - Field-level validation error reporting
4. **API Documentation**: 
   - Complete OpenAPI 3.0 documentation
   - Interactive Swagger UI
   - Schema examples and descriptions
5. **Health Monitoring**: 
   - Custom health indicators for external services
   - Comprehensive application health checks
   - Resilience4j circuit breaker health indicators
6. **Metrics & Observability**:
   - Custom Micrometer metrics
   - Prometheus integration
   - Request timing and success/failure tracking
   - Resilience4j metrics for circuit breaker, rate limiter, and retry
7. **Performance Optimization**:
   - Response caching with Caffeine for frequently accessed data
   - Connection pooling with keep-alive connections
   - Optimized HTTP client configuration
8. **Testing**:
   - Comprehensive unit and integration tests
   - MockMvc integration testing
   - Error scenario coverage
9. **Clean Architecture**: Separation of concerns with Controller-Service-Model layers
10. **Type Safety**: Generic interface implementation with proper type parameters

### Technologies Used

- Spring Boot 3.2.10
- Spring Web
- Spring Cache with Caffeine
- Spring Boot Actuator
- Spring Boot Validation
- SpringDoc OpenAPI 3
- Resilience4j (Circuit Breaker, Rate Limiter, Retry)
- Apache HttpClient 5 (Connection Pooling)
- Micrometer & Prometheus
- Lombok
- Jackson
- JUnit 5
- Mockito
- RestTemplate
- MockMvc

### Design Decisions

1. **RestTemplate**: Used for HTTP communication with retry capabilities
2. **Service Layer**: All business logic isolated in the service layer
3. **DTO Pattern**: Separate models for API requests/responses
4. **Interface Implementation**: Following the provided `IEmployeeController` contract
5. **Immutable Objects**: Using Lombok's `@Builder` for immutable data objects

## Configuration

### Application Properties
- API Port: `8111`
- Mock Server URL: `http://localhost:8112/api/v1/employee`
- HTTP Connection Pool: 100 total connections, 20 per route
- Connection Timeout: 3 seconds
- Read Timeout: 5 seconds

### Resilience Configuration
The application uses Resilience4j patterns with the following configuration:

#### Circuit Breaker
- Sliding window size: 20 requests
- Failure rate threshold: 60%
- Wait duration in open state: 60 seconds
- Calls in half-open state: 5

#### Rate Limiter
- Request limit: 10 requests per second
- Timeout: 2 seconds

#### Retry
- Max attempts: 3
- Wait duration: 1 second
- Exponential backoff: 2x multiplier, max 10 seconds
- Retry exceptions: Server errors, 429 responses, timeouts

### Customization
The base URL for the mock server can be configured via:
```yaml
employee.api.base-url: http://localhost:8112/api/v1/employee
```

Resilience patterns can be tuned in `application.yml`:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      employee-service:
        failure-rate-threshold: 60
        wait-duration-in-open-state: 60s
  ratelimiter:
    instances:
      employee-service:
        limit-for-period: 10
        limit-refresh-period: 1s
```

## Known Limitations

1. The mock server intermittently rate limits requests with 429 responses - handled by resilience patterns
2. Employee deletion requires the employee name (mock server limitation)
3. The mock server allows duplicate employee creation with the same data
4. Circuit breaker fallback returns empty responses for read operations to maintain availability

## Future Enhancements

See [ENHANCEMENTS.md](ENHANCEMENTS.md) for a comprehensive list of potential improvements including:

- Caching implementation with Redis
- Database integration with JPA/Hibernate
- Authentication and authorization (OAuth2/JWT)
- Event-driven architecture with message queues
- Containerization with Docker
- CI/CD pipeline setup
- Load testing and performance optimization

## Available Endpoints Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/employee` | GET | Get all employees |
| `/api/v1/employee/search/{name}` | GET | Search employees by name |
| `/api/v1/employee/{id}` | GET | Get employee by ID |
| `/api/v1/employee/highestSalary` | GET | Get highest salary |
| `/api/v1/employee/topTenHighestEarningEmployeeNames` | GET | Get top 10 earners |
| `/api/v1/employee` | POST | Create new employee |
| `/api/v1/employee/{id}` | DELETE | Delete employee |
| `/swagger-ui.html` | GET | API Documentation |
| `/actuator/health` | GET | Health checks |
| `/actuator/metrics` | GET | Application metrics |