package taxisty.pingtower.backend.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.notifications.config.EmailConfig;
import taxisty.pingtower.backend.notifications.config.TelegramConfig;
import taxisty.pingtower.backend.notifications.config.WebhookConfig;
import taxisty.pingtower.backend.notifications.sender.EmailNotificationSender;
import taxisty.pingtower.backend.notifications.sender.NotificationSender;
import taxisty.pingtower.backend.notifications.sender.TelegramNotificationSender;
import taxisty.pingtower.backend.notifications.sender.WebhookNotificationSender;

import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectMapper objectMapper;
    private final MailSender mailSender;

    private final NotificationSender<TelegramConfig> telegramSender;
    private final NotificationSender<EmailConfig> emailSender;
    private final NotificationSender<WebhookConfig> webhookSender;

    public NotificationService(ObjectMapper objectMapper, MailSender mailSender) {
        this.objectMapper = objectMapper;
        this.mailSender = mailSender;
        this.telegramSender = new TelegramNotificationSender(objectMapper);
        this.emailSender = new EmailNotificationSender(mailSender);
        this.webhookSender = new WebhookNotificationSender(objectMapper);
    }

    public void send(NotificationChannelType type, String configurationJson, NotificationMessage message) {
        try {
            switch (type) {
                case TELEGRAM -> telegramSender.send(message, objectMapper.readValue(configurationJson, TelegramConfig.class));
                case EMAIL -> emailSender.send(message, objectMapper.readValue(configurationJson, EmailConfig.class));
                case WEBHOOK -> webhookSender.send(message, objectMapper.readValue(configurationJson, WebhookConfig.class));
            }
        } catch (Exception e) {
            log.error("Failed to send notification via {}: {}", type, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void send(String type, String configurationJson, NotificationMessage message) {
        send(NotificationChannelType.from(type), configurationJson, message);
    }
}
