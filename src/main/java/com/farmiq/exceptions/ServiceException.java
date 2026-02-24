package com.farmiq.exceptions;

/**
 * Base exception class for all FarmIQ service-layer errors.
 * Provides consistent error handling across the application.
 */
public class ServiceException extends Exception {

    private final String errorCode;

    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
    }

    public ServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
