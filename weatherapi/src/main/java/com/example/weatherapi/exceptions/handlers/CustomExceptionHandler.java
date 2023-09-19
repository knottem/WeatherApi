package com.example.weatherapi.exceptions.handlers;

import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.exceptions.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.exceptions.InvalidCityException;
import com.example.weatherapi.exceptions.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.util.LinkedHashSet;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // Helper method to create error response that can be used in all exception handlers
    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String errorMessage, WebRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .error(errorMessage)
                .status(status.value())
                .path(URLDecoder.decode(request.getDescription(false).substring(4), StandardCharsets.UTF_8))
                .build());
    }

    // Exception handler for all exceptions that are not handled by other exception handlers, might not want to show the exception message to the user
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", request);
    }

    //added decode to get UTF_8, so you can see the proper path in errors
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFoundException(CityNotFoundException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCityException(InvalidCityException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Request body is missing or not readable", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type: " + ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error(ex.getMessage());

        // Get all validation errors and join them into a single string sorted by alphabetical order of the field name,
        // so that the error messages are always in the same order
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(FieldError::getDefaultMessage))
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .map(error -> String.format("%s: %s%s",
                        error.getField().substring(0, 1).toUpperCase() + error.getField().substring(1),
                        formatRejectedValue(error.getRejectedValue()),
                        error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        return createErrorResponse(HttpStatus.BAD_REQUEST, errors, request);
    }

    // Helper method to format rejected value, if the rejected value is null or empty string, it will be ignored
    private String formatRejectedValue(Object rejectedValue) {
        return rejectedValue != null && !(rejectedValue instanceof String && ((String) rejectedValue).trim().isEmpty())
                ? "Invalid value: " + rejectedValue + ", "
                : "";
    }
}
