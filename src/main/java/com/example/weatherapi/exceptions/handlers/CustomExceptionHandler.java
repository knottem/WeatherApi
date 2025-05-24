package com.example.weatherapi.exceptions.handlers;

import com.example.weatherapi.exceptions.*;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // Helper method to create error response that can be used in all exception handlers
    private ProblemDetail createProblemDetail(HttpStatus status, String message, WebRequest request, String type) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(message);
        problem.setProperty("timestamp", ZonedDateTime.now(ZoneId.of("UTC")));
        return problem;
    }

    private String generateTypeFromException(Throwable ex) {
        return "urn:problem-type:" + ex.getClass().getSimpleName()
                .replace("Exception", "")
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
    }

    // Exception handler for all exceptions that are not handled by other exception handlers, might not want to show the exception message to the user
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", request, generateTypeFromException(ex));
    }

    // Exception for InvalidApiUsageException
    @ExceptionHandler(InvalidApiUsageException.class)
    public ProblemDetail handleInvalidApiUsageException(InvalidApiUsageException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), request, generateTypeFromException(ex));
    }

    // Exception handler for ApiDisabledException
    @ExceptionHandler(ApiDisabledException.class)
    public ProblemDetail handleApiDisabledException(ApiDisabledException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        return createProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, generateTypeFromException(ex));
    }

    // Exception handler for ClientAbortException
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex) {
        logger.warn("Client aborted the request: {}", ex.getMessage());
        // Do nothing, just log the exception
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ProblemDetail handleCityNotFoundException(CityNotFoundException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(InvalidCityException.class)
    public ProblemDetail handleInvalidCityException(InvalidCityException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Request body is missing or not readable", request, generateTypeFromException(ex));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type: " + ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(WeatherNotFilledException.class)
    public ProblemDetail handleWeatherNotFilledException(WeatherNotFilledException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request,generateTypeFromException(ex));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        return createProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, generateTypeFromException(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
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

        return createProblemDetail(HttpStatus.BAD_REQUEST, errors, request, generateTypeFromException(ex));
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
