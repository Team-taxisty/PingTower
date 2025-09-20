package taxisty.pingtower.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,
        
        @NotBlank(message = "Password is required")
        String password
) {}