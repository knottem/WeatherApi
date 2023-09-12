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

    //Don't log the error endpoint, since it's where the user is redirected to when they try to access a page they don't have access to
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        String endpoint = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        if (!endpoint.contains("error")) {
            logger.info("User '{}' accessed endpoint: {}", username, endpoint);
        }
        return true;
    }
}
