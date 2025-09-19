package taxisty.pingtower.backend.notifications.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TelegramProvider implements ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(TelegramProvider.class);
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public TelegramProvider(ObjectMapper mapper) {
        this.webClient = WebClient.builder().build();
        this.mapper = mapper;
    }

    @Override
    public String type() { return "TELEGRAM"; }

    @Override
    public DeliveryResult send(Alert alert, NotificationChannel channel) {
        try {
            JsonNode cfg = mapper.readTree(channel.configuration());
            String token = required(cfg, "botToken");
            String chatId = required(cfg, "chatId");
            String parseMode = cfg.path("parseMode").asText("HTML");
            boolean disablePreview = cfg.path("disablePreview").asBoolean(true);

            String text = buildMessage(alert);
            List<String> chunks = splitTelegram(text, 4096);

            DeliveryResult last = null;
            for (String chunk : chunks) {
                last = doSend(token, chatId, chunk, parseMode, disablePreview);
                if (!last.success()) {
                    return last;
                }
            }
            return last == null ? new DeliveryResult(true, 200, null, null) : last;
        } catch (Exception e) {
            log.error("Telegram send failed", e);
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private DeliveryResult doSend(String token, String chatId, String text, String parseMode, boolean disablePreview) {
        try {
            Mono<ClientResponse> respMono = webClient.post()
                    .uri("https://api.telegram.org/bot" + token + "/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{"+
                            "\"chat_id\":\"" + escape(chatId) + "\","+
                            "\"text\":\"" + escape(text) + "\","+
                            "\"parse_mode\":\"" + escape(parseMode) + "\","+
                            "\"disable_web_page_preview\":" + disablePreview +
                            "}"))
                    .exchangeToMono(Mono::just);

            ClientResponse resp = respMono.block(Duration.ofSeconds(15));
            if (resp == null) return new DeliveryResult(false, null, "No response", null);
            int code = resp.rawStatusCode();
            if (code == 200) return new DeliveryResult(true, code, null, null);

            Long retry = null;
            if (code == 429) {
                String ra = resp.headers().asHttpHeaders().getFirst("Retry-After");
                if (ra != null) try { retry = Long.parseLong(ra); } catch (NumberFormatException ignored) {}
            }
            String body = resp.bodyToMono(String.class).block(Duration.ofSeconds(10));
            return new DeliveryResult(false, code, body, retry);
        } catch (Exception e) {
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private static String required(JsonNode n, String field) {
        String v = n.path(field).asText(null);
        if (v == null || v.isEmpty()) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }

    private static String buildMessage(Alert a) {
        String status = a.isResolved() ? "RESOLVED" : "OPEN";
        StringBuilder sb = new StringBuilder();
        sb.append("<b>" + escapeHtml(a.severity()) + "</b> ").append(status).append("\\n");
        sb.append(escapeHtml(a.message())).append("\\n");
        if (a.triggeredAt() != null) {
            sb.append("at: ").append(escapeHtml(String.valueOf(a.triggeredAt()))).append("\\n");
        }
        return sb.toString();
    }

    private static List<String> splitTelegram(String text, int max) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) return List.of(text);
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < bytes.length) {
            int end = Math.min(bytes.length, start + max);
            // do not cut mid-char
            while (end > start && (bytes[end - 1] & 0xC0) == 0x80) end--; // UTF-8 continuation
            parts.add(new String(bytes, start, end - start, StandardCharsets.UTF_8));
            start = end;
        }
        return parts;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

