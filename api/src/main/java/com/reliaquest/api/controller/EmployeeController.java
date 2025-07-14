package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.ErrorResponse;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "APIs for managing employee data")
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private final EmployeeService employeeService;

    @Override
    @Operation(summary = "Get all employees", description = "Retrieves a list of all employees in the system")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved all employees",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array = @ArraySchema(schema = @Schema(implementation = Employee.class)))),
                @ApiResponse(
                        responseCode = "502",
                        description = "External service error",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Getting all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Override
    @Operation(
            summary = "Search employees by name",
            description = "Searches for employees whose names contain the specified search string (case-insensitive)")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved matching employees",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array = @ArraySchema(schema = @Schema(implementation = Employee.class)))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid search parameter",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(
            @Parameter(description = "Name search string", example = "John") String searchString) {
        log.info("Searching employees by name: {}", searchString);
        List<Employee> employees = employeeService.searchEmployeesByName(searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    @Operation(summary = "Get employee by ID", description = "Retrieves a specific employee by their unique identifier")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved the employee",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Employee.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Employee not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<Employee> getEmployeeById(
            @Parameter(description = "Employee UUID", example = "550e8400-e29b-41d4-a716-446655440000") String id) {
        log.info("Getting employee by id: {}", id);
        return employeeService
                .getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Operation(summary = "Get highest salary", description = "Returns the highest salary among all employees")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved the highest salary",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Integer.class, example = "150000")))
            })
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Getting highest salary");
        Integer highestSalary = employeeService.getHighestSalary();
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    @Operation(
            summary = "Get top 10 highest earning employee names",
            description =
                    "Returns the names of the top 10 highest paid employees, sorted by salary in descending order")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved top earners",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array = @ArraySchema(schema = @Schema(implementation = String.class))))
            })
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Getting top 10 highest earning employee names");
        List<String> names = employeeService.getTop10HighestEarningEmployeeNames();
        return ResponseEntity.ok(names);
    }

    @Override
    @Operation(summary = "Create a new employee", description = "Creates a new employee with the provided information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Employee created successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Employee.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid employee data",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<Employee> createEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Employee data to create",
                            required = true,
                            content =
                                    @Content(
                                            schema = @Schema(implementation = CreateEmployeeInput.class),
                                            examples =
                                                    @ExampleObject(
                                                            name = "Sample Employee",
                                                            value =
                                                                    "{\"name\": \"John Smith\", \"salary\": 75000, \"age\": 30, \"title\": \"Software Engineer\"}")))
                    @Valid
                    CreateEmployeeInput employeeInput) {
        log.info("Creating new employee: {}", employeeInput.getName());
        Employee created = employeeService.createEmployee(employeeInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @Operation(
            summary = "Delete employee by ID",
            description = "Deletes an employee by their unique identifier and returns the name of the deleted employee")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Employee deleted successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = String.class, example = "John Smith"))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Employee not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<String> deleteEmployeeById(
            @Parameter(description = "Employee UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
                    String id) {
        log.info("Deleting employee by id: {}", id);
        String deletedEmployeeName = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(deletedEmployeeName);
    }
}
