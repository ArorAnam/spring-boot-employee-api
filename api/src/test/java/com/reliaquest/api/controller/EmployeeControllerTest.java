package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee testEmployee;
    private CreateEmployeeInput createInput;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        createInput = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();
    }

    @Test
    void getAllEmployees_Success() {
        List<Employee> employees = Arrays.asList(
                testEmployee,
                Employee.builder().id(UUID.randomUUID()).name("Jane Doe").build());
        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_Exception() {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        List<Employee> employees = Collections.singletonList(testEmployee);
        when(employeeService.searchEmployeesByName("John")).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("John");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
    }

    @Test
    void getEmployeeById_Found() {
        String id = testEmployee.getId().toString();
        when(employeeService.getEmployeeById(id)).thenReturn(Optional.of(testEmployee));

        ResponseEntity<Employee> response = employeeController.getEmployeeById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEmployee.getName(), response.getBody().getName());
    }

    @Test
    void getEmployeeById_NotFound() {
        String id = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(id)).thenReturn(Optional.empty());

        ResponseEntity<Employee> response = employeeController.getEmployeeById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        when(employeeService.getHighestSalary()).thenReturn(150000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(150000, response.getBody());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        List<String> names = Arrays.asList("John Doe", "Jane Smith", "Bob Johnson");
        when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(names);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void createEmployee_Success() {
        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(testEmployee);

        ResponseEntity<Employee> response = employeeController.createEmployee(createInput);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEmployee.getName(), response.getBody().getName());
    }

    @Test
    void deleteEmployeeById_Success() {
        String id = testEmployee.getId().toString();
        when(employeeService.deleteEmployeeById(id)).thenReturn("John Doe");

        ResponseEntity<String> response = employeeController.deleteEmployeeById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody());
    }

    @Test
    void deleteEmployeeById_NotFound() {
        String id = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(id))
                .thenThrow(new RuntimeException("Employee not found with id: " + id));

        ResponseEntity<String> response = employeeController.deleteEmployeeById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteEmployeeById_OtherError() {
        String id = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(id)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<String> response = employeeController.deleteEmployeeById(id);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
