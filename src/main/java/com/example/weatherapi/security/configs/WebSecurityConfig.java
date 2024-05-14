package com.example.weatherapi.security.configs;

import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.exceptions.handlers.CustomAccessDeniedHandler;
import com.example.weatherapi.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService){
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(r -> r
                .requestMatchers("/error").permitAll()
                .requestMatchers("/weather/**").permitAll()
                .requestMatchers("/actuator/health").hasRole(UserRole.ADMIN.toString())
                .requestMatchers("/city/names").permitAll()
                .requestMatchers("/city/all").hasAnyRole(UserRole.ADMIN.toString(), UserRole.USER.toString())
                .requestMatchers("/city/delete/**").hasRole(UserRole.ADMIN.toString())
                .requestMatchers("/city/create").hasRole(UserRole.ADMIN.toString())
                .requestMatchers("/city/**").hasRole(UserRole.ADMIN.toString())
                .requestMatchers("/auth/**").hasRole(UserRole.ADMIN.toString())
        );

        //Adding custom access denied handler to be able to log unauthorized access attempts.
        http.exceptionHandling(e -> e
                .accessDeniedHandler(customAccessDeniedHandler())
        );

        //Using Basic Auth just for simplicity for now.
        http.httpBasic(withDefaults());

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //Disabling csrf due to being a rest API, and we're already using authentication and cors
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();

        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST"));
        cors.setAllowCredentials(true);
        cors.setAllowedHeaders(List.of("*"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

}