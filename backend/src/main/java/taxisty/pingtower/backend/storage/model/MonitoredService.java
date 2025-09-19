package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a web service or resource being monitored by PingTower.
 * Contains configuration for monitoring checks including URL, intervals, and validation rules.
 */
public record MonitoredService(
        Long id,
        String name,
        String description,
        String url,
        String httpMethod,
        Map<String, String> headers,
        String expectedResponseCode,
        String expectedContent,
        boolean sslCertificateCheck,
        boolean isActive,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}