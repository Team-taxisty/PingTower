package taxisty.pingtower.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        
        @NotBlank(message = "Password is required")
        String password
) {}