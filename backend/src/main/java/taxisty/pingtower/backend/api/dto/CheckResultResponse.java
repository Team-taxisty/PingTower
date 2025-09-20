package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for check result information
 */
public record CheckResultResponse(
        Long id,
        Long serviceId,
        String serviceName,
        LocalDateTime checkedAt,
        Boolean successful,
        Integer responseCode,
        Integer responseTimeMs,
        String errorMessage
) {}