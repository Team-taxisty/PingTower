package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for alert information in API responses.
 * Contains alert details and current status for notification management.
 */
public record AlertResponse(
        Long id,
        Long serviceId,
        String serviceName,
        String message,
        String severity,
        boolean isResolved,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt
) {}