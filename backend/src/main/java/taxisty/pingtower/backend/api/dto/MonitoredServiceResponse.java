package taxisty.pingtower.backend.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for monitored service information
 */
public record MonitoredServiceResponse(
        Long id,
        String name,
        String description,
        String url,
        String serviceType,
        Boolean enabled,
        Integer checkIntervalMinutes,
        Integer timeoutSeconds,
        String httpMethod,
        Map<String, String> headers,
        String requestBody,
        Map<String, String> queryParams,
        Integer expectedStatusCode,
        String expectedResponseBody,
        Boolean isAlive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}