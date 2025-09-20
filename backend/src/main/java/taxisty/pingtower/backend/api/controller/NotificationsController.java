package taxisty.pingtower.backend.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.TestNotificationRequest;
import taxisty.pingtower.backend.api.dto.NotificationRequest;
import taxisty.pingtower.backend.notifications.repo.InMemoryNotificationRepository;
import taxisty.pingtower.backend.notifications.service.NotificationService;
import taxisty.pingtower.backend.storage.dto.CreateNotificationChannelRequest;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;
import taxisty.pingtower.backend.storage.model.NotificationDelivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Уведомления", description = "API для управления каналами уведомлений и отправки уведомлений")
public class NotificationsController {
    private final InMemoryNotificationRepository repo;
    private final NotificationService service;

    public NotificationsController(InMemoryNotificationRepository repo, NotificationService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping("/channels")
    @Operation(summary = "Создать канал уведомлений", description = "Создает новый канал для отправки оповещений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Канал создан успешно"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    public ResponseEntity<NotificationChannel> createChannel(@Valid @RequestBody CreateNotificationChannelRequest req) {
        if (!StringUtils.hasText(req.type()) || !StringUtils.hasText(req.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        NotificationChannel ch = repo.createChannel(1L, req.type(), req.name(), req.configuration(), true, req.isDefault());
        return ResponseEntity.status(HttpStatus.CREATED).body(ch);
    }

    @GetMapping("/channels")
    @Operation(summary = "Получить каналы уведомлений", description = "Возвращает все настроенные каналы уведомлений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Каналы успешно получены")
    })
    public Map<String, Object> listChannels() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", repo.listChannels());
        return resp;
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Получить доставки уведомлений", description = "Возвращает все записи о доставке уведомлений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Записи успешно получены")
    })
    public Map<String, Object> listDeliveries() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", repo.listDeliveries());
        return resp;
    }

    @PostMapping("/test")
    @Operation(summary = "Отправить тестовое уведомление", description = "Отправляет тестовое уведомление для проверки конфигурации канала")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Тестовое уведомление отправлено"),
        @ApiResponse(responseCode = "404", description = "Канал не найден")
    })
    public ResponseEntity<NotificationDelivery> sendTest(@Parameter(description = "ID канала") @RequestParam("channelId") Long channelId,
                                                         @Valid @RequestBody TestNotificationRequest req) {
        NotificationChannel ch = repo.getChannel(channelId);
        if (ch == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Alert alert = new Alert(
                0L,
                0L,
                0L,
                req.getMessage(),
                "INFO",
                false,
                LocalDateTime.now(),
                null,
                Map.of("test", "true")
        );
        NotificationDelivery d = service.sendToChannel(alert, ch);
        return ResponseEntity.ok(d);
    }

    @PostMapping("/send")
    @Operation(summary = "Отправить уведомление", description = "Отправляет уведомление через настроенные каналы")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Уведомление отправлено успешно"),
        @ApiResponse(responseCode = "404", description = "Подходящий канал не найден"),
        @ApiResponse(responseCode = "500", description = "Не удалось отправить уведомление")
    })
    public ResponseEntity<Map<String, Object>> sendNotification(@Valid @RequestBody NotificationRequest req) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Создаем Alert из запроса
            Alert alert = new Alert(
                    0L,
                    0L,
                    0L,
                    req.message(),
                    req.severity() != null ? req.severity() : "INFO",
                    req.status() != null && req.status().equals("up"),
                    LocalDateTime.now(),
                    null,
                    Map.of(
                        "username", req.username(),
                        "service_name", req.serviceName(),
                        "service_url", req.serviceUrl() != null ? req.serviceUrl() : "",
                        "status", req.status() != null ? req.status() : "down"
                    )
            );
            
            // Находим канал уведомлений для пользователя
            NotificationChannel channel = findChannelForUser(req.username());
            if (channel == null) {
                response.put("success", false);
                response.put("error", "No notification channel found for user: " + req.username());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Отправляем уведомление
            NotificationDelivery delivery = service.sendToChannel(alert, channel);
            
            response.put("success", "SUCCESS".equals(delivery.status()));
            response.put("delivery_id", delivery.id());
            response.put("message", "Notification sent successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to send notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private NotificationChannel findChannelForUser(String username) {
        // Ищем канал уведомлений для пользователя
        // Сначала пробуем найти Python Bot канал, затем Telegram
        return repo.listChannels().stream()
                .filter(ch -> ch.type().equals("PYTHON_BOT"))
                .findFirst()
                .orElse(repo.listChannels().stream()
                        .filter(ch -> ch.type().equals("TELEGRAM"))
                        .findFirst()
                        .orElse(null));
    }
}
