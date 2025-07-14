package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    private EmployeeService employeeService;

    private final String baseUrl = "http://localhost:8112/api/v1/employee";
    private Employee testEmployee;
    private ApiResponse<List<Employee>> listResponse;
    private ApiResponse<Employee> singleResponse;

    @BeforeEach
    void setUp() {
        // Create service instance
        employeeService = new EmployeeService(restTemplate, meterRegistry);
        ReflectionTestUtils.setField(employeeService, "baseUrl", baseUrl);

        testEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        listResponse = new ApiResponse<>();
        listResponse.setData(Arrays.asList(testEmployee));
        listResponse.setStatus("Successfully processed request.");

        singleResponse = new ApiResponse<>();
        singleResponse.setData(testEmployee);
        singleResponse.setStatus("Successfully processed request.");
    }

    @Test
    void getAllEmployees_Success() {
        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(listResponse));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class);
                MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);
            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);
            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);
            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            List<Employee> result = employeeService.getAllEmployees();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testEmployee.getName(), result.get(0).getName());
        }
    }

    @Test
    void getAllEmployees_EmptyResponse() {
        ApiResponse<List<Employee>> emptyResponse = new ApiResponse<>();
        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);

            List<Employee> result = employeeService.getAllEmployees();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void searchEmployeesByName_Found() {
        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(listResponse));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class);
                MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);
            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);
            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);
            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            List<Employee> result = employeeService.searchEmployeesByName("John");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("John Doe", result.get(0).getName());
        }
    }

    @Test
    void searchEmployeesByName_NotFound() {
        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(listResponse));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class);
                MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);
            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);
            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);
            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            List<Employee> result = employeeService.searchEmployeesByName("Smith");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getEmployeeById_Found() {
        String id = testEmployee.getId().toString();
        when(restTemplate.exchange(
                        eq(baseUrl + "/" + id), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(singleResponse));

        Optional<Employee> result = employeeService.getEmployeeById(id);

        assertTrue(result.isPresent());
        assertEquals(testEmployee.getName(), result.get().getName());
    }

    @Test
    void getEmployeeById_NotFound() {
        String id = UUID.randomUUID().toString();
        when(restTemplate.exchange(
                        eq(baseUrl + "/" + id), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        Optional<Employee> result = employeeService.getEmployeeById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void getHighestSalary_Success() {
        Employee highEarner = Employee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(150000)
                .build();

        ApiResponse<List<Employee>> response = new ApiResponse<>();
        response.setData(Arrays.asList(testEmployee, highEarner));

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class);
                MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);
            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);
            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);
            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            Integer result = employeeService.getHighestSalary();

            assertEquals(150000, result);
        }
    }

    @Test
    void getTop10HighestEarningEmployeeNames_Success() {
        Employee emp1 = Employee.builder().name("Employee1").salary(100000).build();
        Employee emp2 = Employee.builder().name("Employee2").salary(90000).build();
        Employee emp3 = Employee.builder().name("Employee3").salary(80000).build();

        ApiResponse<List<Employee>> response = new ApiResponse<>();
        response.setData(Arrays.asList(emp2, emp1, emp3));

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class);
                MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Timer.Sample mockSample = mock(Timer.Sample.class);
            Timer.Builder mockTimerBuilder = mock(Timer.Builder.class);
            Timer mockTimer = mock(Timer.class);
            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            timerMock.when(() -> Timer.start(any(MeterRegistry.class))).thenReturn(mockSample);
            timerMock.when(() -> Timer.builder(anyString())).thenReturn(mockTimerBuilder);
            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockTimerBuilder.description(anyString())).thenReturn(mockTimerBuilder);
            when(mockTimerBuilder.register(any(MeterRegistry.class))).thenReturn(mockTimer);
            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            List<String> result = employeeService.getTop10HighestEarningEmployeeNames();

            assertEquals(3, result.size());
            assertEquals("Employee1", result.get(0));
            assertEquals("Employee2", result.get(1));
            assertEquals("Employee3", result.get(2));
        }
    }

    @Test
    void createEmployee_Success() {
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        when(restTemplate.exchange(
                        eq(baseUrl), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(singleResponse));

        try (MockedStatic<Counter> counterMock = mockStatic(Counter.class)) {

            Counter.Builder mockCounterBuilder = mock(Counter.Builder.class);
            Counter mockCounter = mock(Counter.class);

            counterMock.when(() -> Counter.builder(anyString())).thenReturn(mockCounterBuilder);

            when(mockCounterBuilder.description(anyString())).thenReturn(mockCounterBuilder);
            when(mockCounterBuilder.register(any(MeterRegistry.class))).thenReturn(mockCounter);

            Employee result = employeeService.createEmployee(input);

            assertNotNull(result);
            assertEquals(testEmployee.getName(), result.getName());
        }
    }

    @Test
    void deleteEmployeeById_Success() {
        String id = testEmployee.getId().toString();

        // Mock getEmployeeById
        when(restTemplate.exchange(
                        eq(baseUrl + "/" + id), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(singleResponse));

        // Mock delete
        ApiResponse<Boolean> deleteResponse = new ApiResponse<>();
        deleteResponse.setData(true);
        when(restTemplate.exchange(
                        eq(baseUrl),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        String result = employeeService.deleteEmployeeById(id);

        assertEquals("John Doe", result);
    }

    @Test
    void deleteEmployeeById_NotFound() {
        String id = UUID.randomUUID().toString();

        when(restTemplate.exchange(
                        eq(baseUrl + "/" + id), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployeeById(id));
    }
}
