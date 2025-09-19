package taxisty.pingtower.backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.SendNotificationRequest;
import taxisty.pingtower.backend.notifications.NotificationMessage;
import taxisty.pingtower.backend.notifications.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    private final NotificationService notificationService;

    public NotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> test(@RequestBody SendNotificationRequest req) {
        NotificationMessage message = new NotificationMessage(
                req.title(),
                req.text(),
                req.severity(),
                req.link(),
                req.attributes()
        );
        notificationService.send(req.type(), req.configuration(), message);
        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}

