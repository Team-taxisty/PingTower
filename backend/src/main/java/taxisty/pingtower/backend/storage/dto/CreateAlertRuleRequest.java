package taxisty.pingtower.backend.storage.dto;

/**
 * Data transfer object for creating alert rules with threshold configurations.
 * Used to define when and how alerts should be triggered.
 */
public record CreateAlertRuleRequest(
        Long serviceId,
        String name,
        String description,
        int failureThreshold,
        int warningThreshold,
        long responseTimeThresholdMs,
        String severity
) {}