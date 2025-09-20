package taxisty.pingtower.backend.api.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating notification channels
 */
public record NotificationChannelRequest(
        @NotBlank(message = "Channel name is required")
        String name,
        
        @NotBlank(message = "Channel type is required")
        String type,
        
        @NotNull(message = "Configuration is required")
        Map<String, String> configuration,
        
        @NotNull(message = "Enabled status is required")
        Boolean enabled
) {
    public NotificationChannelRequest {
        // Set default if null
        if (enabled == null) {
            enabled = true;
        }
    }
}