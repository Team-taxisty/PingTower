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

import java.time.Duration;
import java.util.Map;

@Component
public class PythonBotProvider implements ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(PythonBotProvider.class);
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public PythonBotProvider(ObjectMapper mapper) {
        this.webClient = WebClient.builder().build();
        this.mapper = mapper;
    }

    @Override
    public String type() { 
        return "PYTHON_BOT"; 
    }

    @Override
    public DeliveryResult send(Alert alert, NotificationChannel channel) {
        try {
            JsonNode cfg = mapper.readTree(channel.configuration());
            String botUrl = required(cfg, "botUrl");
            
            // РР·РІР»РµРєР°РµРј РґР°РЅРЅС‹Рµ РёР· РјРµС‚Р°РґР°РЅРЅС‹С… alert
            Map<String, String> metadata = alert.metadata();
            String username = metadata.get("username");
            String serviceName = metadata.get("service_name");
            String serviceUrl = metadata.get("service_url");
            String status = metadata.get("status");
            
            if (username == null || serviceName == null) {
                return new DeliveryResult(false, null, "Missing username or service_name in alert metadata", null);
            }
            
            // Р¤РѕСЂРјРёСЂСѓРµРј Р·Р°РїСЂРѕСЃ Рє Python Р±РѕС‚Сѓ
            Map<String, Object> requestBody = Map.of(
                "username", (Object) username,
                "service_name", (Object) serviceName,
                "service_url", (Object) (serviceUrl != null ? serviceUrl : ""),
                "status", (Object) (status != null ? status : "down"),
                "message", (Object) alert.message()
            );
            
            return doSend(botUrl, requestBody);
            
        } catch (Exception e) {
            log.error("Python bot send failed", e);
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private DeliveryResult doSend(String botUrl, Map<String, Object> requestBody) {
        try {
            Mono<ClientResponse> respMono = webClient.post()
                    .uri(botUrl + "/send_notification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .exchangeToMono(Mono::just);

            ClientResponse resp = respMono.block(Duration.ofSeconds(15));
            if (resp == null) return new DeliveryResult(false, null, "No response from Python bot", null);
            
            int code = resp.statusCode().value();
            if (code == 200) {
                return new DeliveryResult(true, code, null, null);
            }

            String body = resp.bodyToMono(String.class).block(Duration.ofSeconds(10));
            return new DeliveryResult(false, code, body, null);
            
        } catch (Exception e) {
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private static String required(JsonNode n, String field) {
        String v = n.path(field).asText(null);
        if (v == null || v.isEmpty()) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }
}
