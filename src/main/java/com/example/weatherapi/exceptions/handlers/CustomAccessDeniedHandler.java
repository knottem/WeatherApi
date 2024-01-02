package com.example.weatherapi.exceptions.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("endpoint: ").append(requestURI);
        logMessage.append(" from IP: ").append(request.getHeader("X-Forwarded-For"));
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if(username != null){
            logMessage.append(" by user: ").append(username);
        }
        logger.warn("Unauthorized access attempt to {}", logMessage);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
}
