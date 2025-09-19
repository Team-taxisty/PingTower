package taxisty.pingtower.backend.notifications.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import taxisty.pingtower.backend.notifications.NotificationMessage;
import taxisty.pingtower.backend.notifications.config.WebhookConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class WebhookNotificationSender implements NotificationSender<WebhookConfig> {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public WebhookNotificationSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(NotificationMessage message, WebhookConfig config) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", message.title());
        payload.put("text", message.text());
        payload.put("severity", message.severity());
        payload.put("link", message.link());
        payload.put("attributes", message.attributes());

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.url()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (config.headers() != null) {
            config.headers().forEach(builder::header);
        }
        if (config.secret() != null && !config.secret().isBlank()) {
            builder.header("X-Webhook-Secret", config.secret());
        }
        HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Webhook error: HTTP " + resp.statusCode() + ": " + resp.body());
        }
    }
}

