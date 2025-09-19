package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Defines notification channels and delivery configurations for alerts.
 * Supports multiple delivery methods including email, Telegram, and webhooks.
 */
public record NotificationChannel(
        Long id,
        Long userId,
        String type,
        String name,
        String configuration,
        boolean isEnabled,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}