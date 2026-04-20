package com.fooddeliveryapp.api.exception;

import com.fooddeliveryapp.api.exceptions.LockBusyException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// Lower priority than ApiExceptionHandler – only handles what ApiExceptionHandler doesn't
@Order(100)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Data was modified by another transaction. Please try again.");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Generic fallback – explicitly excludes LockBusyException so ApiExceptionHandler
     * can handle it with HTTP 409.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        // LockBusyException is handled by ApiExceptionHandler with HTTP 409
        if (ex instanceof LockBusyException) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            response.put("retryable", "true");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

