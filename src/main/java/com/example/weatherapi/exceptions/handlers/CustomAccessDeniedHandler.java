package com.example.weatherapi.exceptions.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        if ("/favicon.ico".equals(requestURI)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        StringBuilder logMessage = new StringBuilder()
                .append("endpoint: ")
                .append(requestURI)
                .append(" from IP: ")
                .append(request.getHeader("X-Forwarded-For"));

        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if(username != null){
            logMessage.append(" by user: ").append(username);
        }
        logger.warn("Unauthorized access attempt to {}", logMessage);

        // Create a ProblemDetail with the instance
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("Access Denied");
        problem.setInstance(URI.create(requestURI));
        problem.setProperty("timestamp", ZonedDateTime.now(ZoneId.of("UTC")));

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
