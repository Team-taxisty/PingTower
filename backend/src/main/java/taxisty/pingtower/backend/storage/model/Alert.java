package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a triggered alert notification with delivery status.
 * Tracks notification attempts across different channels (email, Telegram, webhooks).
 */
public record Alert(
        Long id,
        Long alertRuleId,
        Long serviceId,
        String message,
        String severity,
        boolean isResolved,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        Map<String, String> metadata
) {}