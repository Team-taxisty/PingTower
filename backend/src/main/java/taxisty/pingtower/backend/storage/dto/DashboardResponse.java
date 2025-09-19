package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for dashboard statistics and overview metrics.
 * Provides aggregated information for monitoring dashboard displays.
 */
public record DashboardResponse(
        int totalServices,
        int activeServices,
        int servicesUp,
        int servicesDown,
        int activeAlerts,
        double averageUptimePercentage,
        long averageResponseTimeMs,
        LocalDateTime lastUpdateTime
) {}