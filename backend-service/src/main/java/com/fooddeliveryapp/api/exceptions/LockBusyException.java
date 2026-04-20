package com.fooddeliveryapp.api.exceptions;

/**
 * Thrown when a distributed lock cannot be acquired within the specified wait time.
 * Should be mapped to HTTP 409 Conflict by the GlobalExceptionHandler.
 */
public class LockBusyException extends RuntimeException {

    public LockBusyException(String message) {
        super(message);
    }

    public LockBusyException(String message, Throwable cause) {
        super(message, cause);
    }
}
