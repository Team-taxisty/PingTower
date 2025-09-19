package taxisty.pingtower.backend.storage.dto;

/**
 * Data transfer object for creating monitoring schedules.
 * Allows flexible configuration of check intervals and timing.
 */
public record CreateScheduleRequest(
        Long serviceId,
        String cronExpression,
        int intervalSeconds,
        String timezone
) {}