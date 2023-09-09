package com.example.weatherapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptLogger {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptLogger.class);

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        logger.info("Login successful, Username: {}, Role: {}",
                ((UserDetails) event.getAuthentication().getPrincipal()).getUsername(),
                event.getAuthentication().getAuthorities().toString().replace("[", "").replace("]", "").substring(5));
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        logger.warn("Login failed, Username: {}", event.getAuthentication().getPrincipal());
    }
}