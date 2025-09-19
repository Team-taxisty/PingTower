package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for service information in API responses.
 * Provides essential service details without sensitive configuration data.
 */
public record ServiceResponse(
        Long id,
        String name,
        String description,
        String url,
        String status,
        double uptimePercentage,
        long lastResponseTimeMs,
        LocalDateTime lastCheckTime,
        LocalDateTime createdAt
) {}