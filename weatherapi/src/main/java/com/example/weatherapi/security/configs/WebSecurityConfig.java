package com.example.weatherapi.security.configs;

import com.example.weatherapi.exceptions.handlers.CustomAccessDeniedHandler;
import com.example.weatherapi.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    @Value("${app.test-mode}")
    private boolean testMode;

    //Setting up security for the endpoints.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //Setting roles for endpoints, nothing should be accessible without a role,
        // except for the error endpoint which is where the user is redirected to when they try to access a page they don't have access to.
        http.authorizeHttpRequests(r -> r
                .requestMatchers("/error").permitAll()
                .requestMatchers("/weather/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/city/**").hasRole("ADMIN")
                .requestMatchers("/addCity/**").hasRole("ADMIN")
        );

        //Adding custom access denied handler to be able to log unauthorized access attempts.
        http.exceptionHandling(e -> e
                .accessDeniedHandler(customAccessDeniedHandler())
        );

        //Using Basic Auth just for simplicity for now.
        http.httpBasic(withDefaults());

        //If test mode is on, disable csrf.
        if(testMode){
            http.csrf(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

}