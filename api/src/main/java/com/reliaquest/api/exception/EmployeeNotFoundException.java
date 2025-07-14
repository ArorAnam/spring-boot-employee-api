package com.reliaquest.api.exception;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends RuntimeException {

    private final String employeeId;

    public EmployeeNotFoundException(String employeeId) {
        super(String.format("Employee not found with ID: %s", employeeId));
        this.employeeId = employeeId;
    }

    public EmployeeNotFoundException(String employeeId, Throwable cause) {
        super(String.format("Employee not found with ID: %s", employeeId), cause);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}
