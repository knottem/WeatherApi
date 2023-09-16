package com.example.weatherapi.exceptions.handlers;

import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.exceptions.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.exceptions.InvalidCityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@ControllerAdvice
public class CustomExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /*
        Make my custom exceptions handlers always return the same way as Spring boot errors
        ResponseEntity.status(HttpStatus HERE).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus HERE)
                .path(URLDecoder.decode(request.getDescription(false).substring(4), StandardCharsets.UTF_8))
                .build());

        The only different thing should be the HttpStatus on each.

        Made custom exceptions, so I can have different error messages for different situations.
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(URLDecoder.decode(request.getDescription(false).substring(4), StandardCharsets.UTF_8))
                .build());
    }

    //added decode to get UTF_8, so you can see the proper path in errors
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFoundException(CityNotFoundException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .path(URLDecoder.decode(request.getDescription(false).substring(4), StandardCharsets.UTF_8))
                .build());
    }

    @ExceptionHandler(InvalidCityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCityException(InvalidCityException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(URLDecoder.decode(request.getDescription(false).substring(4), StandardCharsets.UTF_8))
                .build());
    }
}
