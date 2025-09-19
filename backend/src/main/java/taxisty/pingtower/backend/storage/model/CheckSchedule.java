package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Defines the monitoring schedule for a service using cron-like configuration.
 * Supports flexible intervals from seconds to hours based on service criticality.
 */
public record CheckSchedule(
        Long id,
        Long serviceId,
        String cronExpression,
        int intervalSeconds,
        boolean isEnabled,
        String timezone,
        LocalDateTime nextRunTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}