package taxisty.pingtower.backend.storage.dto;

import java.util.Map;

/**
 * Data transfer object for creating or updating a monitored service.
 * Used in REST API requests for service configuration.
 */
public record CreateServiceRequest(
        String name,
        String description,
        String url,
        String httpMethod,
        Map<String, String> headers,
        String expectedResponseCode,
        String expectedContent,
        boolean sslCertificateCheck
) {}