package taxisty.pingtower.backend.api.dto;

import java.util.Map;

public record SendNotificationRequest(
        String type,
        String configuration,
        String title,
        String text,
        String severity,
        String link,
        Map<String, Object> attributes
) {}

