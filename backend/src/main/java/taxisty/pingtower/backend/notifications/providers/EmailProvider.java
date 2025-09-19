package taxisty.pingtower.backend.notifications.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;

import java.time.Duration;
import java.util.Properties;

@Component
public class EmailProvider implements ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(EmailProvider.class);
    private final ObjectMapper mapper;

    public EmailProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String type() { return "EMAIL"; }

    @Override
    public DeliveryResult send(Alert alert, NotificationChannel channel) {
        try {
            JsonNode cfg = mapper.readTree(channel.configuration());
            String host = required(cfg, "smtpHost");
            int port = cfg.path("smtpPort").asInt(587);
            String username = cfg.path("username").asText(null);
            String password = cfg.path("password").asText(null);
            String from = required(cfg, "from");
            String to = required(cfg, "to");
            boolean useStartTLS = cfg.path("useStartTLS").asBoolean(true);
            boolean useSSL = cfg.path("useSSL").asBoolean(false);

            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(port);
            if (username != null) sender.setUsername(username);
            if (password != null) sender.setPassword(password);
            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", String.valueOf(username != null));
            props.put("mail.smtp.starttls.enable", String.valueOf(useStartTLS));
            props.put("mail.smtp.ssl.enable", String.valueOf(useSSL));
            props.put("mail.smtp.connectiontimeout", String.valueOf(Duration.ofSeconds(10).toMillis()));
            props.put("mail.smtp.timeout", String.valueOf(Duration.ofSeconds(15).toMillis()));
            props.put("mail.smtp.writetimeout", String.valueOf(Duration.ofSeconds(15).toMillis()));

            MimeMessage msg = sender.createMimeMessage();
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(buildSubject(alert));
            String html = buildHtml(alert);
            msg.setContent(html, "text/html; charset=UTF-8");

            sender.send(msg);
            return new DeliveryResult(true, 250, null, null);
        } catch (Exception e) {
            log.error("Email send failed", e);
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private static String required(JsonNode n, String field) {
        String v = n.path(field).asText(null);
        if (v == null || v.isEmpty()) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }

    private static String buildSubject(Alert a) {
        return (a.isResolved() ? "RESOLVED " : "ALERT ") + a.severity() + ": service=" + a.serviceId();
    }

    private static String buildHtml(Alert a) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>")
                .append(a.isResolved() ? "Incident Resolved" : "Incident Opened")
                .append(" - ")
                .append(escape(a.severity()))
                .append("</h3>");
        sb.append("<p>").append(escape(a.message())).append("</p>")
                .append("<ul>")
                .append("<li>Service ID: ").append(a.serviceId()).append("</li>")
                .append("<li>Triggered: ").append(escape(String.valueOf(a.triggeredAt()))).append("</li>")
                .append("<li>Resolved: ").append(escape(String.valueOf(a.resolvedAt()))).append("</li>")
                .append("</ul>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

