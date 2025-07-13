# ReliaQuest Employee Challenge - Development Instructions

## **Prerequisites & Setup**

### **Java Version Requirements**
This project requires **Java 17**. Set your environment:

```bash
# Check available Java versions
/usr/libexec/java_home -V

# Set Java 17 for current session
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Verify correct version
java -version  # Should show "openjdk version 17.x.x"
```

### **Project Structure**
- **`server/`** - Mock Employee API (Port 8112) - **DO NOT MODIFY**
- **`api/`** - Your implementation (Port 8111) - **TO BE IMPLEMENTED**

---

## **Core Gradle Commands**

### **Running Applications**
```bash
# Start Mock Employee Server (REQUIRED - keep running)
./gradlew server:bootRun          # Port 8112

# Start Your API Implementation (once built)
./gradlew api:bootRun             # Port 8111
```

### **Building & Compiling**
```bash
# Clean build artifacts
./gradlew clean                   # Clean everything
./gradlew api:clean               # Clean API module only

# Compile code
./gradlew compileJava             # Compile all modules
./gradlew api:compileJava         # Compile API module only

# Full build (compile + test + package)
./gradlew build                   # Build entire project
./gradlew api:build               # Build API module only
./gradlew server:build            # Build server module only
```

### **Testing**
```bash
# Run tests
./gradlew test                    # All tests
./gradlew api:test                # API module tests only
./gradlew server:test             # Server module tests only

# Continuous testing (re-runs on file changes)
./gradlew api:test --continuous
```

### **Code Formatting (REQUIRED)**
```bash
# Fix formatting issues (MUST run before final submission)
./gradlew spotlessApply

# Check formatting without fixing
./gradlew spotlessCheck
```

### **Development Tools**
```bash
./gradlew tasks                   # List all available tasks
./gradlew dependencies            # Show dependency tree
./gradlew api:dependencies        # API module dependencies

# Build with detailed information
./gradlew build --info           # Detailed logging
./gradlew build --scan           # Online build report
```

---

## **Testing the Mock Server**

### **Basic API Calls**
```bash
# Get all employees
curl http://localhost:8112/api/v1/employee | jq

# Get specific employee (replace {id} with actual UUID)
curl http://localhost:8112/api/v1/employee/{employee-id} | jq

# Create new employee
curl -X POST http://localhost:8112/api/v1/employee \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "salary": 75000,
    "age": 30,
    "title": "Software Developer"
  }' | jq

# Delete employee (by name in request body)
curl -X DELETE http://localhost:8112/api/v1/employee \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe"}' | jq
```

### **Expected Response Format**
```json
{
  "data": [
    {
      "id": "uuid-string",
      "employee_name": "Tiger Nixon",
      "employee_salary": 320800,
      "employee_age": 61,
      "employee_title": "Job Title",
      "employee_email": "email@company.com"
    }
  ],
  "status": "Successfully processed request."
}
```

---

## **Development Workflow**

### **1. Implementation Requirements**
You need to create these components in `api/src/main/java/com/reliaquest/api/`:

```
├── model/
│   ├── Employee.java              # Your employee entity
│   └── CreateEmployeeRequest.java # Request DTO for creation
├── service/
│   └── EmployeeService.java       # Business logic
├── client/
│   └── MockEmployeeClient.java    # HTTP client to call mock server
└── controller/
    └── EmployeeController.java    # Implements IEmployeeController
```

### **2. Controller Interface to Implement**
Your controller must implement `IEmployeeController<Entity, Input>` with these endpoints:

- `GET /` → `getAllEmployees()`
- `GET /search/{searchString}` → `getEmployeesByNameSearch()`
- `GET /{id}` → `getEmployeeById()`
- `GET /highestSalary` → `getHighestSalaryOfEmployees()`
- `GET /topTenHighestEarningEmployeeNames` → `getTopTenHighestEarningEmployeeNames()`
- `POST /` → `createEmployee()`
- `DELETE /{id}` → `deleteEmployeeById()`

### **3. Development Loop**
```bash
# 1. Write/modify code

# 2. Compile to check syntax
./gradlew api:compileJava

# 3. Run tests
./gradlew api:test

# 4. Fix code formatting
./gradlew spotlessApply

# 5. Full build
./gradlew api:build

# 6. Start your API (when ready)
./gradlew api:bootRun
```

### **4. Testing Your Implementation**
Once your API is running on port 8111:

```bash
# Test your endpoints
curl http://localhost:8111/getAllEmployees
curl http://localhost:8111/search/John
curl http://localhost:8111/12345678-1234-1234-1234-123456789abc
curl http://localhost:8111/highestSalary
curl http://localhost:8111/topTenHighestEarningEmployeeNames

# Create employee
curl -X POST http://localhost:8111/ \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","salary":80000,"age":28,"title":"Designer"}'

# Delete employee
curl -X DELETE http://localhost:8111/employee-id-here
```

---

## **Key Implementation Challenges**

### **1. API Mismatch**
- **Challenge**: Your API deletes by ID, but mock server deletes by name
- **Solution**: Fetch employee by ID first to get name, then delete by name

### **2. Rate Limiting**
- **Challenge**: Mock server randomly rate limits (5-10 requests, then 30-90s cooldown)
- **Solution**: Implement retry logic and proper error handling

### **3. Data Transformation**
- **Challenge**: Mock server uses `snake_case` (`employee_name`), your API should use `camelCase`
- **Solution**: Create DTOs and mapping logic

### **4. Error Handling**
- Handle 404s for non-existent employees
- Handle 429 (Too Many Requests) from rate limiting
- Proper HTTP status codes and error messages

---

## **Assessment Criteria**

Your implementation will be evaluated on:

- ✅ **Clean coding practices**
- ✅ **Test-driven development** (unit & integration tests)
- ✅ **Proper logging** implementation
- ✅ **Scalability** considerations
- ✅ **Error handling** and resilience
- ✅ **Code formatting** (Spotless compliance)

---

## **Important Notes**

1. **Keep Server Running**: Always keep `./gradlew server:bootRun` running during development

2. **Java Version**: Always ensure Java 17 is active:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

3. **Clean Build**: If you encounter build issues:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

4. **Code Formatting**: Always run before submission:
   ```bash
   ./gradlew spotlessApply
   ```

5. **Fresh Data**: Each server restart generates new fake employee data

6. **Port Configuration**:
   - Mock Server: `http://localhost:8112`
   - Your API: `http://localhost:8111`

---

## **Quick Start Checklist**

- [ ] Java 17 installed and active
- [ ] Server running: `./gradlew server:bootRun`
- [ ] Server tested: `curl http://localhost:8112/api/v1/employee`
- [ ] API module compiles: `./gradlew api:compileJava`
- [ ] Code formatting works: `./gradlew spotlessApply`
- [ ] Ready to implement!

---

**Good luck with your implementation!** 🚀 