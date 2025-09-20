package taxisty.pingtower.backend.api.dto;

import java.util.Map;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating or updating a monitored service
 */
public record MonitoredServiceRequest(
        @NotBlank(message = "Service name is required")
        String name,
        
        @NotBlank(message = "URL is required")
        @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
        String url,
        
        @NotNull(message = "Service type is required")
        String type,
        
        @NotNull(message = "Enabled status is required")
        Boolean enabled,
        
        @Min(value = 1, message = "Check interval must be at least 1 minute")
        @Max(value = 1440, message = "Check interval cannot exceed 1440 minutes (24 hours)")
        Integer checkIntervalMinutes,
        
        @Min(value = 1, message = "Timeout must be at least 1 second")
        @Max(value = 300, message = "Timeout cannot exceed 300 seconds")
        Integer timeoutSeconds,
        
        @Min(value = 100, message = "Expected status code must be a valid HTTP status code")
        @Max(value = 599, message = "Expected status code must be a valid HTTP status code")
        Integer expectedStatusCode,
        
        Map<String, String> headers
) {
    public MonitoredServiceRequest {
        // Set defaults if null
        if (enabled == null) {
            enabled = true;
        }
        if (checkIntervalMinutes == null) {
            checkIntervalMinutes = 5;
        }
        if (timeoutSeconds == null) {
            timeoutSeconds = 30;
        }
        if (expectedStatusCode == null) {
            expectedStatusCode = 200;
        }
    }
}