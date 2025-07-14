package com.reliaquest.api.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_Success() throws Exception {
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("John Doe")
                        .salary(75000)
                        .age(30)
                        .title("Software Engineer")
                        .email("john.doe@company.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Jane Smith")
                        .salary(85000)
                        .age(28)
                        .title("Senior Developer")
                        .email("jane.smith@company.com")
                        .build());

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")))
                .andExpect(jsonPath("$[0].employee_salary", is(75000)))
                .andExpect(jsonPath("$[1].employee_name", is("Jane Smith")))
                .andExpect(jsonPath("$[1].employee_salary", is(85000)));
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        List<Employee> employees = Arrays.asList(Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .build());

        when(employeeService.searchEmployeesByName("John")).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/search/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        UUID employeeId = UUID.randomUUID();
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        when(employeeService.getEmployeeById(employeeId.toString())).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(employeeId.toString())))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(75000)));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(employeeService.getEmployeeById(employeeId.toString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/employee/" + employeeId)).andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(150000);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(150000)));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        List<String> topEarners = Arrays.asList("John Doe", "Jane Smith", "Bob Johnson");
        when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(topEarners);

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("John Doe")))
                .andExpect(jsonPath("$[1]", is("Jane Smith")))
                .andExpect(jsonPath("$[2]", is("Bob Johnson")));
    }

    @Test
    void createEmployee_Success() throws Exception {
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        Employee createdEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(75000)))
                .andExpect(jsonPath("$.employee_age", is(30)))
                .andExpect(jsonPath("$.employee_title", is("Software Engineer")));
    }

    @Test
    void createEmployee_ValidationError() throws Exception {
        CreateEmployeeInput invalidInput = CreateEmployeeInput.builder()
                .name("") // Invalid: blank name
                .salary(-1000) // Invalid: negative salary
                .age(200) // Invalid: age over 75
                .title("") // Invalid: blank title
                .build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))));
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(employeeService.deleteEmployeeById(employeeId.toString())).thenReturn("John Doe");

        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("John Doe"));
    }

    @Test
    void invalidJsonFormat_BadRequest() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("INVALID_JSON")))
                .andExpect(jsonPath("$.message", containsString("Invalid JSON format")));
    }

    @Test
    void methodNotAllowed_Error() throws Exception {
        mockMvc.perform(patch("/api/v1/employee"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("METHOD_NOT_SUPPORTED")));
    }
}
