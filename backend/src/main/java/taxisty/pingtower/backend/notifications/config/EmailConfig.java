package taxisty.pingtower.backend.notifications.config;

import java.util.List;
import java.util.Map;

public record EmailConfig(
        String from,
        List<String> to,
        String subjectPrefix,
        Map<String, String> smtp // host, port, username, password, tls ("true"/"false")
) {}

