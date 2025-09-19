package taxisty.pingtower.backend.notifications;

import java.util.Map;

public record NotificationMessage(
        String title,
        String text,
        String severity, // INFO, WARN, CRITICAL
        String link,
        Map<String, Object> attributes
) {
}

