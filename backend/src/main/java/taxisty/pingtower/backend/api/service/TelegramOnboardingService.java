package taxisty.pingtower.backend.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * Lightweight helper that notifies the Python bot about a newly created user.
 */
@Service
public class TelegramOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramOnboardingService.class);

    private final WebClient webClient;
    private final String baseUrl;

    public TelegramOnboardingService(WebClient.Builder webClientBuilder,
                                     @Value("${pingtower.telegram.bot-service-url:http://localhost:5000}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl != null && baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    public void prepareLink(Long userId, String username) {
        if (userId == null || username == null || username.isBlank()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "username", username,
                "token", String.valueOf(userId)
        );

        try {
            webClient.post()
                    .uri(baseUrl + "/generate_link")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(Duration.ofSeconds(5));
        } catch (WebClientResponseException e) {
            log.warn("Bot rejected Telegram link provisioning for user {} (status {}): {}", username,
                    e.getRawStatusCode(), e.getResponseBodyAsString());
        } catch (Exception ex) {
            log.warn("Failed to provision Telegram link for user {}: {}", username, ex.getMessage());
        }
    }
}