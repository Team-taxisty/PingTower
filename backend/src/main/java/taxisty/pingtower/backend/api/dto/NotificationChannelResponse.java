package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for notification channel information
 */
public record NotificationChannelResponse(
        Long id,
        String name,
        String type,
        Map<String, String> configuration,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}