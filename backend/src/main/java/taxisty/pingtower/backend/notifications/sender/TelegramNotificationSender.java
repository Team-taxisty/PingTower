package taxisty.pingtower.backend.notifications.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import taxisty.pingtower.backend.notifications.NotificationMessage;
import taxisty.pingtower.backend.notifications.config.TelegramConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TelegramNotificationSender implements NotificationSender<TelegramConfig> {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public TelegramNotificationSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(NotificationMessage message, TelegramConfig config) throws Exception {
        String url = "https://api.telegram.org/bot" + config.botToken() + "/sendMessage";

        String text = formatText(message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", config.chatId());
        payload.put("text", text);
        payload.put("parse_mode", "HTML");

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Telegram API error: HTTP " + resp.statusCode() + ": " + resp.body());
        }
    }

    private String formatText(NotificationMessage m) {
        StringBuilder sb = new StringBuilder();
        if (m.severity() != null) {
            sb.append("<b>").append(m.severity()).append("</b> ");
        }
        if (m.title() != null) {
            sb.append("<b>").append(escape(m.title())).append("</b>\n");
        }
        if (m.text() != null) {
            sb.append(escape(m.text())).append("\n");
        }
        if (m.link() != null) {
            sb.append("\n").append(escape(m.link())).append("\n");
        }
        if (m.attributes() != null && !m.attributes().isEmpty()) {
            m.attributes().forEach((k, v) -> sb.append("<code>").append(escape(k)).append(": ")
                    .append(escape(String.valueOf(v))).append("</code>\n"));
        }
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

