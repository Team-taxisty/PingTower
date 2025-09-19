package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;

/**
 * Contains the results of a service availability check including response metrics.
 * Stores historical data for analytics and reporting purposes.
 */
public record CheckResult(
        Long id,
        Long serviceId,
        LocalDateTime checkTime,
        boolean isSuccessful,
        int responseCode,
        long responseTimeMs,
        String responseBody,
        String errorMessage,
        boolean sslValid,
        LocalDateTime sslExpiryDate,
        String checkLocation
) {}