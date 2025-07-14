package com.reliaquest.api.exception;

/**
 * Exception thrown when there's an error communicating with external services.
 */
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error communicating with %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = -1;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error communicating with %s: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.statusCode = -1;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super(String.format("Error communicating with %s (HTTP %d): %s", serviceName, statusCode, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message, Throwable cause) {
        super(String.format("Error communicating with %s (HTTP %d): %s", serviceName, statusCode, message), cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
