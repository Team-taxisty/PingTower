package taxisty.pingtower.backend.notifications.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.notifications.api.dto.TestNotificationRequest;
import taxisty.pingtower.backend.notifications.repo.InMemoryNotificationRepository;
import taxisty.pingtower.backend.notifications.service.NotificationService;
import taxisty.pingtower.backend.storage.dto.CreateNotificationChannelRequest;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;
import taxisty.pingtower.backend.storage.model.NotificationDelivery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    private final InMemoryNotificationRepository repo;
    private final NotificationService service;

    public NotificationsController(InMemoryNotificationRepository repo, NotificationService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping("/channels")
    public ResponseEntity<NotificationChannel> createChannel(@Valid @RequestBody CreateNotificationChannelRequest req) {
        if (!StringUtils.hasText(req.type()) || !StringUtils.hasText(req.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        NotificationChannel ch = repo.createChannel(1L, req.type(), req.name(), req.configuration(), true, req.isDefault());
        return ResponseEntity.status(HttpStatus.CREATED).body(ch);
    }

    @GetMapping("/channels")
    public Map<String, Object> listChannels() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", repo.listChannels());
        return resp;
    }

    @GetMapping("/deliveries")
    public Map<String, Object> listDeliveries() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", repo.listDeliveries());
        return resp;
    }

    @PostMapping("/test")
    public ResponseEntity<NotificationDelivery> sendTest(@RequestParam("channelId") Long channelId,
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
}

