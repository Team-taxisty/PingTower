package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for alert information
 */
public record AlertResponse(
        Long id,
        Long serviceId,
        String message,
        String severity,
        Boolean resolved,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        Map<String, String> metadata
) {}