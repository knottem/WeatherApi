package com.example.weatherapi.exceptions.handlers;

import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.exceptions.*;
import org.apache.catalina.connector.ClientAbortException;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // Helper method to create error response that can be used in all exception handlers
    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String errorMessage, WebRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
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

    // Exception for InvalidApiUsageException
    @ExceptionHandler(InvalidApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiUsageException(InvalidApiUsageException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Exception handler for ApiDisabledException
    @ExceptionHandler(ApiDisabledException.class)
    public ResponseEntity<ErrorResponse> handleApiDisabledException(ApiDisabledException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    // Exception handler for ClientAbortException
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex) {
        logger.warn("Client aborted the request: {}", ex.getMessage());
        // Do nothing, just log the exception
    }

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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(WeatherNotFilledException.class)
    public ResponseEntity<ErrorResponse> handleWeatherNotFilledException(WeatherNotFilledException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        // Get all validation errors and join them into a single string sorted by alphabetical order of the field name,
        // so that the error messages are always in the same order
        // only show the first error message for each field, so that the error messages are not too long
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField))
                .values().stream()
                .map(fieldErrors -> fieldErrors.stream().min(Comparator.comparing(FieldError::getDefaultMessage))
                        .map(error -> formatRejectedValue(error) + error.getDefaultMessage())
                        .orElse(""))
                .collect(Collectors.joining(", "));

        // Log the validation errors
        logger.error("Validation failed: {}", errors);

        return createErrorResponse(HttpStatus.BAD_REQUEST, errors, request);
    }

    // Helper method to format rejected value, if the rejected value is null or empty string, it will be ignored
    // If the rejected value is password, it will be ignored as well
    private String formatRejectedValue(FieldError error) {
        if ("password".equalsIgnoreCase(error.getField())) {
            return "";
        }

        Object rejectedValue = error.getRejectedValue();
        return rejectedValue != null && !(rejectedValue instanceof String && ((String) rejectedValue).trim().isEmpty())
                ? "Invalid value: " + rejectedValue + ", "
                : "";
    }
}
