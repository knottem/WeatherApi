package com.example.weatherapi.security.loggers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        logger.info("User '{}' accessed endpoint: {}", username,URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8));
        return true;
    }
}
