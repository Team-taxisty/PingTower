package taxisty.pingtower.backend.notifications.sender;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSender;
import taxisty.pingtower.backend.notifications.NotificationMessage;
import taxisty.pingtower.backend.notifications.config.EmailConfig;

import java.util.List;

public class EmailNotificationSender implements NotificationSender<EmailConfig> {
    private final MailSender mailSender;

    public EmailNotificationSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(NotificationMessage message, EmailConfig config) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(config.from());
        List<String> recipients = config.to();
        mail.setTo(recipients.toArray(new String[0]));

        String subjectPrefix = config.subjectPrefix() == null ? "" : config.subjectPrefix().trim() + " ";
        String subject = subjectPrefix + (message.title() == null ? "Notification" : message.title());
        if (message.severity() != null) {
            subject = "[" + message.severity() + "] " + subject;
        }
        mail.setSubject(subject);

        StringBuilder body = new StringBuilder();
        if (message.text() != null) body.append(message.text()).append("\n\n");
        if (message.link() != null) body.append("Link: ").append(message.link()).append("\n");
        if (message.attributes() != null && !message.attributes().isEmpty()) {
            body.append("\nDetails:\n");
            message.attributes().forEach((k, v) -> body.append(" - ").append(k).append(": ").append(String.valueOf(v)).append("\n"));
        }
        mail.setText(body.toString());

        mailSender.send(mail);
    }
}
