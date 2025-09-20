package taxisty.pingtower.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "Username is required")
        String username,
        
        @NotBlank(message = "Service name is required")
        String serviceName,
        
        String serviceUrl,
        
        String status, // "up" or "down"
        
        String severity, // "INFO", "WARNING", "ERROR", "CRITICAL"
        
        @NotBlank(message = "Message is required")
        String message
) {}
