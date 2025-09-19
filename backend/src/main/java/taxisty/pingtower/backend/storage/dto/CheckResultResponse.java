package taxisty.pingtower.backend.storage.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for check result information in API responses.
 * Provides monitoring check outcomes and performance metrics.
 */
public record CheckResultResponse(
        Long id,
        Long serviceId,
        String serviceName,
        LocalDateTime checkTime,
        boolean isSuccessful,
        int responseCode,
        long responseTimeMs,
        String errorMessage,
        boolean sslValid,
        LocalDateTime sslExpiryDate
) {}