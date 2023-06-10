package com.example.weatherapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    //Setting up security for the endpoints.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //Setting roles for endpoints, nothing should be accessible without a role.
        http.authorizeHttpRequests(r -> r
                .requestMatchers("/weather/**").hasRole("USER")
                .requestMatchers("/city/**").hasRole("USER"));

        //Using Basic Auth just for simplicity for now.
        http.httpBasic(withDefaults());

        //Setting up a custom authentication failure handler.
        //TODO - Fix getRequestURI() to return the correct path and not just /error.
        http.exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
            logger.error("Unauthorized access attempt: " + authException.getMessage());
            logger.error("IP: " + request.getRemoteAddr() + " attempted to access: " + request.getRequestURI() + " with method: " + request.getMethod() + " and user agent: " + request.getHeader("User-Agent"));
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }));

        return http.build();
    }

    //Setting up a user with a role. More users can be added here, but for now we only need one.
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername(username)
                .password(passwordEncoder().encode(password))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    //Using BCrypt for password encoding.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}