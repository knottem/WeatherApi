package com.example.weatherapi.exceptions.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            logger.warn("Basic Auth failed for request to {} from IP: {}", requestURI, ip);
        }

        response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setContentLength(0);
        response.flushBuffer();
    }
}