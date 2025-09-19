package taxisty.pingtower.backend.notifications.config;

import java.util.Map;

public record WebhookConfig(
        String url,
        String secret,
        Map<String, String> headers
) {}

