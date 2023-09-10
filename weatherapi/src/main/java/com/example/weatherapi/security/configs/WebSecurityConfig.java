package com.example.weatherapi.security.configs;

import com.example.weatherapi.exceptions.handlers.CustomAccessDeniedHandler;
import com.example.weatherapi.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    //Setting up security for the endpoints.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //Setting roles for endpoints, nothing should be accessible without a role.
        http.authorizeHttpRequests(r -> r
                .requestMatchers("/error").permitAll()
                .requestMatchers("/weather/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/city/**").hasRole("ADMIN")
        );

        http.exceptionHandling(e -> e
                .accessDeniedHandler(customAccessDeniedHandler())
        );

        //Using Basic Auth just for simplicity for now.
        http.httpBasic(withDefaults());

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

}