package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.exceptions.LockBusyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * HTTP 409 Conflict – distributed lock could not be acquired.
     * Android client should show: "Hệ thống đang xử lý, vui lòng thử lại."
     */
    @ExceptionHandler(LockBusyException.class)
    public ResponseEntity<?> lockBusy(LockBusyException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", e.getMessage(),
                        "retryable", true
                ));
    }

    /** HTTP 400 – generic business-rule violations (e.g. out-of-stock). */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> missingPart(MissingServletRequestPartException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "missing multipart field: " + e.getRequestPartName()));
    }

    @ExceptionHandler(com.fooddeliveryapp.api.exceptions.ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFound(com.fooddeliveryapp.api.exceptions.ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> validationError(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .findFirst().orElse("Dữ liệu không hợp lệ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    @ExceptionHandler(com.fooddeliveryapp.api.exceptions.BadRequestException.class)
    public ResponseEntity<?> badRequest(com.fooddeliveryapp.api.exceptions.BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}

