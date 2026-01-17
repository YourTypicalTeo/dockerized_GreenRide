package com.greenride.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long with a digit, upper, lower and special character")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.*]).*$",
                message = "Password must contain a digit, lowercase, uppercase, and a special character")
        String password,

        @Pattern(regexp = "^\\+3069\\d{8}$", 
         message = "Phone number must be a valid Greek mobile (e.g., +3069XXXXXXXX)")
        String phoneNumber
) {}
