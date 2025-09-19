package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Aggregated metrics for service availability and performance reporting.
 * Used for dashboard visualization and SLA reporting.
 */
public record ServiceMetrics(
        Long id,
        Long serviceId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        double uptimePercentage,
        long averageResponseTimeMs,
        long maxResponseTimeMs,
        long minResponseTimeMs,
        int totalChecks,
        int successfulChecks,
        int failedChecks,
        String aggregationPeriod
) {}