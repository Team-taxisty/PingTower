package taxisty.pingtower.backend.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Facade that asks the Python bot for one-time Telegram deep-link URLs.
 */
@Service
public class TelegramOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramOnboardingService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public TelegramOnboardingService(WebClient.Builder webClientBuilder,
                                     ObjectMapper objectMapper,
                                     @Value("${pingtower.telegram.bot-service-url:http://localhost:5000}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl != null && baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    public TelegramLinkDetails generateLink(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            String body = webClient.post()
                    .uri(baseUrl + "/generate_link")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("username", username))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            if (body == null || body.isBlank()) {
                log.warn("Empty response while generating Telegram link for user {}", username);
                return null;
            }

            JsonNode node = objectMapper.readTree(body);
            return new TelegramLinkDetails(
                    node.path("link").asText(null),
                    node.path("token").asText(null),
                    node.path("already_linked").asBoolean(false),
                    node.path("expires_at").asText(null),
                    node.path("ttl_hours").isNumber() ? node.path("ttl_hours").asInt() : null,
                    node.path("chat_id").isNumber() ? node.path("chat_id").asLong() : null,
                    node.path("bot_username").asText(null)
            );
        } catch (Exception ex) {
            log.warn("Failed to generate Telegram link for user {}: {}", username, ex.getMessage());
            return null;
        }
    }

    public record TelegramLinkDetails(
            String link,
            String token,
            boolean alreadyLinked,
            String expiresAt,
            Integer ttlHours,
            Long chatId,
            String botUsername
    ) {}
}