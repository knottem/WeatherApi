package com.example.weatherapi.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth {

    @NotBlank(message = "Username cannot be null or empty")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters long")
    private String username;

    @NotBlank(message = "Password cannot be null or empty")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING)
    private UserRole role;


}
