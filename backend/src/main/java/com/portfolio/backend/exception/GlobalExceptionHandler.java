package com.portfolio.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String message = "Validation failed";
        return buildValidationErrorResponse(errors, message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations()
                .forEach(violation -> errors.add(violation.getPropertyPath() + " " + violation.getMessage()));

        String message = "Validation failed";
        return buildValidationErrorResponse(errors, message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    public ResponseEntity<Object> handleNotFound(
            RuntimeException ex,
            WebRequest request) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), message));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleUnexpected(
            Exception ex,
            WebRequest request) {

        String message = "An unexpected error occurred";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
    }

    private ResponseEntity<Object> buildValidationErrorResponse(
            List<String> errors, String message, HttpStatus status) {

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                message);

        if (errors != null && !errors.isEmpty()) {
            errorResponse.setErrors(errors.stream()
                    .collect(Collectors.toMap(
                            error -> error.split(" ")[0],
                            error -> error.substring(error.indexOf(" ") + 1)
                    )));
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    public static class ErrorResponse {
        private int status;
        private String message;
        private java.util.Map<String, String> errors;

        public ErrorResponse() {
        }

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public java.util.Map<String, String> getErrors() {
            return errors;
        }

        public void setErrors(java.util.Map<String, String> errors) {
            this.errors = errors;
        }
    }
}