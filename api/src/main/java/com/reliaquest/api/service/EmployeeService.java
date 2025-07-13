package com.reliaquest.api.service;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class EmployeeService {

    private final RestTemplate restTemplate;

    @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}")
    private String baseUrl;

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<Employee> getAllEmployees() {
        try {
            log.debug("Fetching all employees from {}", baseUrl);
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully fetched {} employees",
                        response.getBody().getData().size());
                return response.getBody().getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching all employees", e);
            throw new RuntimeException("Failed to fetch employees", e);
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

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public Optional<Employee> getEmployeeById(String id) {
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
            throw new RuntimeException("Failed to fetch employee", e);
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

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public Employee createEmployee(CreateEmployeeInput input) {
        try {
            log.debug("Creating employee: {}", input);
            HttpEntity<CreateEmployeeInput> request = new HttpEntity<>(input);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully created employee: {}",
                        response.getBody().getData().getId());
                return response.getBody().getData();
            }
            throw new RuntimeException("Failed to create employee - no data in response");
        } catch (Exception e) {
            log.error("Error creating employee", e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public String deleteEmployeeById(String id) {
        try {
            // First, get the employee to find their name
            Optional<Employee> employee = getEmployeeById(id);
            if (employee.isEmpty()) {
                throw new RuntimeException("Employee not found with id: " + id);
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
            throw new RuntimeException("Failed to delete employee");
        } catch (Exception e) {
            log.error("Error deleting employee with id: {}", id, e);
            throw new RuntimeException("Failed to delete employee", e);
        }
    }
}
