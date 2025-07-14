package com.reliaquest.api.exception;

import java.util.List;

/**
 * Exception thrown when employee data validation fails.
 */
public class InvalidEmployeeDataException extends RuntimeException {

    private final List<String> validationErrors;

    public InvalidEmployeeDataException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public InvalidEmployeeDataException(List<String> validationErrors) {
        super("Invalid employee data: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public InvalidEmployeeDataException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = List.of(message);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
