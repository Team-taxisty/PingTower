package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object for service metrics and performance analytics responses.
 * Provides historical data and statistics for reporting and visualization.
 */
public record MetricsResponse(
        Long serviceId,
        String serviceName,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        double uptimePercentage,
        long averageResponseTimeMs,
        long maxResponseTimeMs,
        long minResponseTimeMs,
        int totalChecks,
        int successfulChecks,
        int failedChecks,
        List<TimeSeriesPoint> timeSeriesData
) {
    /**
     * Represents a single point in time series data for visualization.
     */
    public record TimeSeriesPoint(
            LocalDateTime timestamp,
            boolean isUp,
            long responseTimeMs
    ) {}
}