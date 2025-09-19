package taxisty.pingtower.backend.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.notifications.model.ChannelType;
import taxisty.pingtower.backend.notifications.providers.ChannelProvider;
import taxisty.pingtower.backend.notifications.providers.EmailProvider;
import taxisty.pingtower.backend.notifications.providers.TelegramProvider;
import taxisty.pingtower.backend.notifications.providers.WebhookProvider;
import taxisty.pingtower.backend.notifications.repo.InMemoryNotificationRepository;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;
import taxisty.pingtower.backend.storage.model.NotificationDelivery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final InMemoryNotificationRepository repo;
    private final TelegramProvider telegram;
    private final EmailProvider email;
    private final WebhookProvider webhook;

    public NotificationService(InMemoryNotificationRepository repo,
                               TelegramProvider telegram,
                               EmailProvider email,
                               WebhookProvider webhook) {
        this.repo = repo;
        this.telegram = telegram;
        this.email = email;
        this.webhook = webhook;
    }

    public void sendAlert(Alert alert) {
        List<NotificationChannel> channels = repo.listChannels();
        for (NotificationChannel ch : channels) {
            if (!ch.isEnabled()) continue;
            try {
                sendToChannel(alert, ch);
            } catch (Exception e) {
                log.error("Failed to send alert to channel {}", ch.id(), e);
            }
        }
    }

    public NotificationDelivery sendToChannel(Alert alert, NotificationChannel ch) {
        ChannelType type = ChannelType.fromString(ch.type());
        ChannelProvider.DeliveryResult res;
        String method;
        if (type == ChannelType.TELEGRAM) {
            method = "TELEGRAM";
            res = telegram.send(alert, ch);
        } else if (type == ChannelType.EMAIL) {
            method = "EMAIL";
            res = email.send(alert, ch);
        } else if (type == ChannelType.WEBHOOK) {
            method = "WEBHOOK";
            res = webhook.send(alert, ch);
        } else {
            throw new IllegalArgumentException("Unsupported channel type: " + ch.type());
        }

        NotificationDelivery delivery = new NotificationDelivery(
                null,
                alert.id(),
                ch.id(),
                res.success() ? "SENT" : "FAILED",
                method,
                1,
                res.error(),
                LocalDateTime.now(),
                res.success() ? LocalDateTime.now() : null
        );
        repo.addDelivery(delivery);
        return delivery;
    }
}

