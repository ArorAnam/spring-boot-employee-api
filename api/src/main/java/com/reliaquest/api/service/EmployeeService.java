package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;

    @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}")
    private String baseUrl;

    @Retry(name = "employee-service", fallbackMethod = "getAllEmployeesFallback")
    @CircuitBreaker(name = "employee-service", fallbackMethod = "getAllEmployeesFallback")
    @RateLimiter(name = "employee-service")
    @Cacheable(value = "employees", unless = "#result.isEmpty()")
    public List<Employee> getAllEmployees() {
        return fetchAllEmployees();
    }

    public List<Employee> getAllEmployeesFallback(Exception e) {
        log.error("Circuit breaker fallback for getAllEmployees. Error: {}", e.getMessage());
        return Collections.emptyList();
    }

    private List<Employee> fetchAllEmployees() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.debug("Fetching all employees from {}", baseUrl);
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                int employeeCount = response.getBody().getData().size();
                log.info("Successfully fetched {} employees", employeeCount);

                // Record metrics
                meterRegistry.gauge("employees.count", employeeCount);
                Counter.builder("employees.fetch.success")
                        .description("Number of successful employee fetch operations")
                        .register(meterRegistry)
                        .increment();

                return response.getBody().getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            Counter.builder("employees.fetch.error")
                    .description("Number of failed employee fetch operations")
                    .register(meterRegistry)
                    .increment();
            log.error("Error fetching all employees", e);
            throw new ExternalServiceException("Mock Employee API", "Failed to fetch employees", e);
        } finally {
            sample.stop(Timer.builder("employees.fetch.duration")
                    .description("Time taken to fetch employees")
                    .register(meterRegistry));
        }
    }

    public List<Employee> searchEmployeesByName(String searchString) {
        log.debug("Searching employees by name: {}", searchString);
        List<Employee> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .filter(emp ->
                        emp.getName() != null && emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Retry(name = "employee-service", fallbackMethod = "getEmployeeByIdFallback")
    @CircuitBreaker(name = "employee-service", fallbackMethod = "getEmployeeByIdFallback")
    @RateLimiter(name = "employee-service")
    @Cacheable(value = "employee-by-id", key = "#id", unless = "#result.isEmpty()")
    public Optional<Employee> getEmployeeById(String id) {
        return fetchEmployeeById(id);
    }

    public Optional<Employee> getEmployeeByIdFallback(String id, Exception e) {
        log.error("Circuit breaker fallback for getEmployeeById {}. Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    private Optional<Employee> fetchEmployeeById(String id) {
        try {
            log.debug("Fetching employee by id: {}", id);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info("Successfully fetched employee with id: {}", id);
                return Optional.of(response.getBody().getData());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee not found with id: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching employee by id: {}", id, e);
            throw new ExternalServiceException("Mock Employee API", "Failed to fetch employee", e);
        }
    }

    public Integer getHighestSalary() {
        log.debug("Calculating highest salary");
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .map(Employee::getSalary)
                .filter(salary -> salary != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> getTop10HighestEarningEmployeeNames() {
        log.debug("Getting top 10 highest earning employees");
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(emp -> emp.getSalary() != null)
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    @Retry(name = "employee-service", fallbackMethod = "createEmployeeFallback")
    @CircuitBreaker(name = "employee-service", fallbackMethod = "createEmployeeFallback")
    @RateLimiter(name = "employee-service")
    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(CreateEmployeeInput input) {
        return performCreateEmployee(input);
    }

    public Employee createEmployeeFallback(CreateEmployeeInput input, Exception e) {
        log.error("Circuit breaker fallback for createEmployee. Error: {}", e.getMessage());
        throw new ExternalServiceException("Mock Employee API", "Service unavailable - circuit breaker open", e);
    }

    private Employee performCreateEmployee(CreateEmployeeInput input) {
        try {
            log.debug("Creating employee: {}", input);
            HttpEntity<CreateEmployeeInput> request = new HttpEntity<>(input);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully created employee: {}",
                        response.getBody().getData().getId());

                // Record metrics
                Counter.builder("employees.created")
                        .description("Number of employees created")
                        .register(meterRegistry)
                        .increment();

                return response.getBody().getData();
            }
            throw new ExternalServiceException("Mock Employee API", "Failed to create employee - no data in response");
        } catch (Exception e) {
            Counter.builder("employees.create.error")
                    .description("Number of failed employee creation operations")
                    .register(meterRegistry)
                    .increment();
            log.error("Error creating employee", e);
            throw new ExternalServiceException("Mock Employee API", "Failed to create employee", e);
        }
    }

    @Retry(name = "employee-service", fallbackMethod = "deleteEmployeeByIdFallback")
    @CircuitBreaker(name = "employee-service", fallbackMethod = "deleteEmployeeByIdFallback")
    @RateLimiter(name = "employee-service")
    @CacheEvict(
            value = {"employees", "employee-by-id"},
            allEntries = true)
    public String deleteEmployeeById(String id) {
        return performDeleteEmployee(id);
    }

    public String deleteEmployeeByIdFallback(String id, Exception e) {
        log.error("Circuit breaker fallback for deleteEmployeeById {}. Error: {}", id, e.getMessage());
        throw new ExternalServiceException("Mock Employee API", "Service unavailable - circuit breaker open", e);
    }

    private String performDeleteEmployee(String id) {
        try {
            // First, get the employee to find their name
            Optional<Employee> employee = fetchEmployeeById(id);
            if (employee.isEmpty()) {
                throw new EmployeeNotFoundException(id);
            }

            String employeeName = employee.get().getName();
            log.debug("Deleting employee with id: {} and name: {}", id, employeeName);

            // The mock API expects name in request body for delete
            Map<String, String> deleteRequest = new HashMap<>();
            deleteRequest.put("name", employeeName);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(deleteRequest);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, request, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().getData())) {
                log.info("Successfully deleted employee with id: {}", id);
                return employeeName;
            }
            throw new ExternalServiceException("Mock Employee API", "Failed to delete employee");
        } catch (EmployeeNotFoundException e) {
            throw e; // Re-throw as-is
        } catch (Exception e) {
            log.error("Error deleting employee with id: {}", id, e);
            throw new ExternalServiceException("Mock Employee API", "Failed to delete employee", e);
        }
    }
}
