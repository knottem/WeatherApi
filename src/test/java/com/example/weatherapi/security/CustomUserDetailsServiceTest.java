package com.example.weatherapi.security;

import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.repositories.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    /**
     * Test Case 1: Check that loadUserByUsername returns the correct UserDetails object.
     * <p>
     * Arrange: An AuthEntity object is created and the repository is mocked to return this object.<br>
     * Act: The loadUserByUsername method is called.<br>
     * Assert: The returned UserDetails object has the correct username, password, and authority.
     */
    @Test
    void shouldReturnCorrectUserDetailsWhenUserExists() {
        // Arrange
        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername("testuser");
        authEntity.setPassword("testpassword");
        authEntity.setRole(UserRole.USER);
        when(authRepository.findByUsername("testuser")).thenReturn(Optional.of(authEntity));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("testpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    /**
     * Test Case 2: Check that loadUserByUsername throws UsernameNotFoundException when the user does not exist.
     * <p>
     * Arrange: The repository is mocked to return an empty Optional.<br>
     * Act & Assert: The loadUserByUsername method is called and an exception is expected to be thrown.
     */
    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        // Arrange
        when(authRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("testuser");
        });
    }
}