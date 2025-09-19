package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Defines alert rules and conditions for triggering notifications.
 * Supports threshold-based alerts and escalation configurations.
 */
public record AlertRule(
        Long id,
        Long serviceId,
        String name,
        String description,
        int failureThreshold,
        int warningThreshold,
        long responseTimeThresholdMs,
        boolean isEnabled,
        String severity,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}