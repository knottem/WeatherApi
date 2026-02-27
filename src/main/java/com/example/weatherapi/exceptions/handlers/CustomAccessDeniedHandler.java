package com.example.weatherapi.exceptions.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        if ("/favicon.ico".equals(requestURI)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        logger.warn("Unauthorized access attempt to endpoint: {} from IP: {}{}",
                requestURI, ip, username != null ? " by user: " + username : "");
        // Return 401 Unauthorized with no response body
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setContentLength(0);
        response.flushBuffer();
    }
}
