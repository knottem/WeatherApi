package com.example.weatherapi.security.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptLogger {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptLogger.class);

     //Disabled this for now since it's not needed for the project, already handling what logged-in users do in LoggingInterceptor.java
     // If you want to see login attempts, uncomment the code below.
    /*
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        logger.info("Login successful, Username: {}, Role: {}",
                ((UserDetails) event.getAuthentication().getPrincipal()).getUsername(),
                event.getAuthentication().getAuthorities().toString().replace("[", "").replace("]", "").substring(5));
    }

     */

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        logger.warn("Login failed, Username: {}", event.getAuthentication().getPrincipal());
    }
}