package taxisty.pingtower.backend.notifications.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class WebhookProvider implements ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(WebhookProvider.class);
    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper mapper;

    public WebhookProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String type() { return "WEBHOOK"; }

    @Override
    public DeliveryResult send(Alert alert, NotificationChannel channel) {
        try {
            JsonNode cfg = mapper.readTree(channel.configuration());
            String url = required(cfg, "url");
            Map<String, String> headers = readHeaders(cfg.path("headers"));
            String hmacSecret = cfg.path("hmacSecret").asText(null);
            String signatureHeader = cfg.path("signatureHeader").asText("X-Signature-256");
            String algo = cfg.path("algo").asText("HmacSHA256");

            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", alert.id());
            payload.put("type", alert.isResolved() ? "IncidentResolved" : "IncidentOpened");
            payload.put("serviceId", alert.serviceId());
            payload.put("severity", alert.severity());
            payload.put("message", alert.message());
            payload.put("triggeredAt", alert.triggeredAt());
            payload.put("resolvedAt", alert.resolvedAt());
            payload.put("metadata", alert.metadata());

            String body = mapper.writeValueAsString(payload);

            WebClient.RequestBodySpec spec = webClient.post().uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.forEach(spec::header);

            if (hmacSecret != null && !hmacSecret.isEmpty()) {
                String sig = sign(algo, hmacSecret, body);
                spec.header(signatureHeader, sig);
            }

            Mono<ClientResponse> respMono = spec.bodyValue(body).exchangeToMono(Mono::just);
            ClientResponse resp = respMono.block(Duration.ofSeconds(15));
            if (resp == null) return new DeliveryResult(false, null, "No response", null);
            int code = resp.rawStatusCode();
            if (code >= 200 && code < 300) return new DeliveryResult(true, code, null, null);
            Long retry = null;
            if (code == 429) {
                String ra = resp.headers().asHttpHeaders().getFirst("Retry-After");
                if (ra != null) try { retry = Long.parseLong(ra); } catch (NumberFormatException ignored) {}
            }
            String respBody = resp.bodyToMono(String.class).block(Duration.ofSeconds(10));
            return new DeliveryResult(false, code, respBody, retry);
        } catch (Exception e) {
            log.error("Webhook send failed", e);
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private static String required(JsonNode n, String field) {
        String v = n.path(field).asText(null);
        if (v == null || v.isEmpty()) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }

    private static Map<String, String> readHeaders(JsonNode node) {
        Map<String, String> map = new HashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                map.put(e.getKey(), e.getValue().asText(""));
            }
        }
        return map;
    }

    private static String sign(String algo, String secret, String body) throws Exception {
        Mac mac = Mac.getInstance(algo);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algo));
        byte[] raw = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + Base64.getEncoder().encodeToString(raw);
    }
}
