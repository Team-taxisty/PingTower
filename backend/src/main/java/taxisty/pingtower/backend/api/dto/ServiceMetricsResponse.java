package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for service metrics and analytics
 */
public record ServiceMetricsResponse(
        Long serviceId,
        String serviceName,
        String serviceUrl,
        Long totalChecks,
        Long successfulChecks,
        Long failedChecks,
        Double averageResponseTime,
        Integer minResponseTime,
        Integer maxResponseTime,
        Double uptimePercentage,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
) {}