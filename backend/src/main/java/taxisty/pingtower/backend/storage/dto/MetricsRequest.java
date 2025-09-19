package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for service metrics and analytics requests.
 * Specifies time range and aggregation level for reporting queries.
 */
public record MetricsRequest(
        Long serviceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String aggregationPeriod
) {}