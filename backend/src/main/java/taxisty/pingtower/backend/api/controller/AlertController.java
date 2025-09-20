package taxisty.pingtower.backend.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import taxisty.pingtower.backend.api.dto.AlertResponse;
import taxisty.pingtower.backend.api.dto.NotificationChannelRequest;
import taxisty.pingtower.backend.api.dto.NotificationChannelResponse;
import taxisty.pingtower.backend.monitoring.repository.AlertRepository;
import taxisty.pingtower.backend.monitoring.repository.NotificationChannelRepository;
import taxisty.pingtower.backend.notifications.service.NotificationService;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;

/**
 * REST API controller for alerts and notifications management
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Оповещения", description = "API для управления оповещениями и каналами уведомлений")
public class AlertController {

    private final AlertRepository alertRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationService notificationService;

    public AlertController(
            AlertRepository alertRepository,
            NotificationChannelRepository notificationChannelRepository,
            NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all alerts with filtering options
     */
    @GetMapping("/alerts")
    @Operation(summary = "Получить все оповещения", description = "Возвращает список оповещений с возможностью фильтрации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Оповещения успешно получены")
    })
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            Pageable pageable,
            @Parameter(description = "ID сервиса для фильтрации") @RequestParam(required = false) Long serviceId,
            @Parameter(description = "Фильтр по разрешенным оповещениям") @RequestParam(required = false) Boolean resolved,
            @Parameter(description = "Уровень серьезности") @RequestParam(required = false) String severity,
            @Parameter(description = "Дата начала периода") @RequestParam(required = false) String since) {
        
        Page<Alert> alerts;
        LocalDateTime sinceTime = parseDateTimeParam(since, LocalDateTime.now().minusDays(30));
        
        // Apply filters based on parameters
        if (serviceId != null && resolved != null && severity != null) {
            alerts = alertRepository.findByServiceIdAndIsResolvedAndSeverityAndTriggeredAtAfter(
                    serviceId, resolved, severity, sinceTime, pageable);
        } else if (serviceId != null && resolved != null) {
            alerts = alertRepository.findByServiceIdAndIsResolvedAndTriggeredAtAfter(
                    serviceId, resolved, sinceTime, pageable);
        } else if (serviceId != null) {
            alerts = alertRepository.findByServiceIdAndTriggeredAtAfter(serviceId, sinceTime, pageable);
        } else if (resolved != null) {
            alerts = alertRepository.findByIsResolvedAndTriggeredAtAfter(resolved, sinceTime, pageable);
        } else {
            alerts = alertRepository.findByTriggeredAtAfter(sinceTime, pageable);
        }

        Page<AlertResponse> response = alerts.map(this::convertToAlertResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific alert by ID
     */
    @GetMapping("/alerts/{id}")
    @Operation(summary = "Получить оповещение по ID", description = "Возвращает детали конкретного оповещения")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Оповещение найдено"),
        @ApiResponse(responseCode = "404", description = "Оповещение не найдено")
    })
    public ResponseEntity<AlertResponse> getAlert(@Parameter(description = "ID оповещения") @PathVariable Long id) {
        Optional<Alert> alert = alertRepository.findById(id);
        
        if (alert.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToAlertResponse(alert.get()));
    }

    /**
     * Mark an alert as resolved
     */
    @PutMapping("/alerts/{id}/resolve")
    @Operation(summary = "Разрешить оповещение", description = "Отмечает оповещение как разрешенное")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Оповещение разрешено"),
        @ApiResponse(responseCode = "404", description = "Оповещение не найдено")
    })
    public ResponseEntity<AlertResponse> resolveAlert(@Parameter(description = "ID оповещения") @PathVariable Long id) {
        Optional<Alert> alertOpt = alertRepository.findById(id);
        
        if (alertOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Alert alert = alertOpt.get();
        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert = alertRepository.save(alert);

        return ResponseEntity.ok(convertToAlertResponse(alert));
    }

    /**
     * Delete an alert
     */
    @DeleteMapping("/alerts/{id}")
    @Operation(summary = "Удалить оповещение", description = "Удаляет оповещение из системы")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Оповещение удалено"),
        @ApiResponse(responseCode = "404", description = "Оповещение не найдено")
    })
    public ResponseEntity<Void> deleteAlert(@Parameter(description = "ID оповещения") @PathVariable Long id) {
        if (!alertRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        alertRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all notification channels
     */
    @GetMapping("/notifications/channels")
    @Operation(summary = "Получить каналы уведомлений", description = "Возвращает список всех каналов уведомлений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Каналы успешно получены")
    })
    public ResponseEntity<List<NotificationChannelResponse>> getNotificationChannels() {
        List<NotificationChannel> channels = notificationChannelRepository.findAll();
        List<NotificationChannelResponse> response = channels.stream()
                .map(this::convertToChannelResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new notification channel
     */
    @PostMapping("/notifications/channels")
    @Operation(summary = "Создать канал уведомлений", description = "Создает новый канал для отправки уведомлений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Канал создан успешно"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<NotificationChannelResponse> createNotificationChannel(
            @Valid @RequestBody NotificationChannelRequest request) {
        
        NotificationChannel channel = convertToChannelEntity(request);
        channel = notificationChannelRepository.save(channel);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToChannelResponse(channel));
    }

    /**
     * Update a notification channel
     */
    @PutMapping("/notifications/channels/{id}")
    @Operation(summary = "Обновить канал уведомлений", description = "Обновляет настройки существующего канала уведомлений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Канал обновлен успешно"),
        @ApiResponse(responseCode = "404", description = "Канал не найден"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<NotificationChannelResponse> updateNotificationChannel(
            @Parameter(description = "ID канала") @PathVariable Long id,
            @Valid @RequestBody NotificationChannelRequest request) {
        
        Optional<NotificationChannel> existingChannel = notificationChannelRepository.findById(id);
        
        if (existingChannel.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NotificationChannel channel = existingChannel.get();
        updateChannelFromRequest(channel, request);
        channel = notificationChannelRepository.save(channel);

        return ResponseEntity.ok(convertToChannelResponse(channel));
    }

    /**
     * Delete a notification channel
     */
    @DeleteMapping("/notifications/channels/{id}")
    @Operation(summary = "Удалить канал уведомлений", description = "Удаляет канал уведомлений из системы")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Канал удален"),
        @ApiResponse(responseCode = "404", description = "Канал не найден")
    })
    public ResponseEntity<Void> deleteNotificationChannel(@Parameter(description = "ID канала") @PathVariable Long id) {
        if (!notificationChannelRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        notificationChannelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test a notification channel
     */
    @PostMapping("/notifications/channels/{id}/test")
    @Operation(summary = "Тестировать канал уведомлений", description = "Отправляет тестовое уведомление через выбранный канал")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Тестовое уведомление отправлено"),
        @ApiResponse(responseCode = "404", description = "Канал не найден"),
        @ApiResponse(responseCode = "500", description = "Ошибка отправки")
    })
    public ResponseEntity<String> testNotificationChannel(@Parameter(description = "ID канала") @PathVariable Long id) {
        Optional<NotificationChannel> channelOpt = notificationChannelRepository.findById(id);
        
        if (channelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            NotificationChannel channel = channelOpt.get();
            
            // Create a test alert for notification
            Alert testAlert = createTestAlert(channel);
            
            // Use NotificationService to send test notification
            notificationService.sendToChannel(testAlert, channel);
            
            return ResponseEntity.ok("Test notification sent successfully to " + channel.getName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Test notification failed: " + e.getMessage());
        }
    }

    private AlertResponse convertToAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getServiceId(),
                alert.getMessage(),
                alert.getSeverity(),
                alert.isResolved(),
                alert.getTriggeredAt(),
                alert.getResolvedAt(),
                alert.getMetadata()
        );
    }

    private NotificationChannelResponse convertToChannelResponse(NotificationChannel channel) {
        // Parse JSON configuration string to Map (simplified approach)
        Map<String, String> configMap = parseConfiguration(channel.getConfiguration());
        
        return new NotificationChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getType(),
                configMap,
                channel.isEnabled(),
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }
    
    private Map<String, String> parseConfiguration(String configJson) {
        // Simplified configuration parsing - in production you'd use Jackson
        Map<String, String> config = new HashMap<>();
        if (configJson != null && !configJson.isBlank()) {
            try {
                // Very basic JSON parsing for demo purposes
                configJson = configJson.replaceAll("[{}\"]", "");
                String[] pairs = configJson.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        config.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            } catch (Exception e) {
                // Return empty map if parsing fails
            }
        }
        return config;
    }

    private NotificationChannel convertToChannelEntity(NotificationChannelRequest request) {
        NotificationChannel channel = new NotificationChannel();
        updateChannelFromRequest(channel, request);
        return channel;
    }

    private void updateChannelFromRequest(NotificationChannel channel, NotificationChannelRequest request) {
        channel.setName(request.name());
        channel.setType(request.type());
        channel.setConfiguration(convertConfigurationToJson(request.configuration()));
        channel.setIsEnabled(request.enabled());
    }
    
    private String convertConfigurationToJson(Map<String, String> config) {
        // Simple JSON conversion - in production you'd use Jackson
        if (config == null || config.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : config.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            json.append("\"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private LocalDateTime parseDateTimeParam(String dateTimeStr, LocalDateTime defaultValue) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return defaultValue;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
    
    /**
     * Creates a test alert for notification testing
     */
    private Alert createTestAlert(NotificationChannel channel) {
        Alert testAlert = new Alert();
        testAlert.setServiceId(-1L); // Use -1 to indicate test alert
        testAlert.setMessage("This is a test notification from PingTower monitoring system for channel: " + channel.getName());
        testAlert.setSeverity("INFO");
        testAlert.setIsResolved(false);
        testAlert.setTriggeredAt(LocalDateTime.now());
        
        // Add test metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("testAlert", "true");
        metadata.put("channelType", channel.getType());
        metadata.put("channelName", channel.getName());
        metadata.put("timestamp", LocalDateTime.now().toString());
        testAlert.setMetadata(metadata);
        
        return testAlert;
    }
}