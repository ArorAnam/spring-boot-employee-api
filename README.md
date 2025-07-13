# Employee Management API

A Spring Boot application that provides RESTful APIs for managing employee data. This project implements a proxy API that interacts with a mock employee server and provides additional business logic and data manipulation capabilities.

## Table of Contents
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
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

## Testing

### Unit Tests
The project includes comprehensive unit tests for:
- Controller layer (`EmployeeControllerTest`)
- Service layer (`EmployeeServiceTest`)

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

1. **Retry Mechanism**: Automatic retry with exponential backoff for handling rate limiting (429 responses)
2. **Error Handling**: Comprehensive error handling with appropriate HTTP status codes
3. **Validation**: Input validation using Jakarta Bean Validation
4. **Logging**: Structured logging at appropriate levels
5. **Clean Architecture**: Separation of concerns with Controller-Service-Model layers
6. **Type Safety**: Generic interface implementation with proper type parameters

### Technologies Used

- Spring Boot 2.7.x
- Spring Web
- Spring Retry
- Lombok
- Jackson
- JUnit 5
- Mockito
- RestTemplate

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
- Timeouts: 5 seconds for connection and read

### Customization
The base URL for the mock server can be configured via:
```yaml
employee.api.base-url: http://localhost:8112/api/v1/employee
```

## Known Limitations

1. The mock server randomly rate limits requests - the API handles this with retries
2. Employee deletion requires the employee name (mock server limitation)
3. The mock server allows duplicate employee creation with the same data

## Future Enhancements

- Add caching for frequently accessed data
- Implement pagination for employee list
- Add API versioning
- Enhance with OpenAPI/Swagger documentation
- Add metrics and monitoring
- Implement authentication and authorization
- Add more comprehensive integration tests