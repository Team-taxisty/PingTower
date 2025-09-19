package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Tracks notification delivery attempts and their status.
 * Maintains audit trail for notification reliability and debugging.
 */
public record NotificationDelivery(
        Long id,
        Long alertId,
        Long channelId,
        String status,
        String deliveryMethod,
        int attemptCount,
        String errorMessage,
        LocalDateTime sentAt,
        LocalDateTime deliveredAt
) {}