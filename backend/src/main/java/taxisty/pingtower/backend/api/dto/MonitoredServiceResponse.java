package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for monitored service information
 */
public record MonitoredServiceResponse(
        Long id,
        String name,
        String url,
        String type,
        Boolean enabled,
        Integer checkIntervalMinutes,
        Integer timeoutSeconds,
        Integer expectedStatusCode,
        Map<String, String> headers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}