package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a triggered alert notification with delivery status.
 * Tracks notification attempts across different channels (email, Telegram, webhooks).
 */
@Entity
@Table(name = "alert")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alert_rule_id")
    private Long alertRuleId;
    
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "severity", length = 50)
    private String severity = "MEDIUM";
    
    @Column(name = "is_resolved")
    private boolean isResolved = false;
    
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, String> metadata = new HashMap<>();
    
    public Alert() {}
    
    public Alert(Long id, Long alertRuleId, Long serviceId, String message, 
                String severity, boolean isResolved, LocalDateTime triggeredAt, 
                LocalDateTime resolvedAt, Map<String, String> metadata) {
        this.id = id;
        this.alertRuleId = alertRuleId;
        this.serviceId = serviceId;
        this.message = message;
        this.severity = severity;
        this.isResolved = isResolved;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAlertRuleId() { return alertRuleId; }
    public void setAlertRuleId(Long alertRuleId) { this.alertRuleId = alertRuleId; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public boolean isResolved() { return isResolved; }
    public void setIsResolved(boolean isResolved) { this.isResolved = isResolved; }
    
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long alertRuleId() { return alertRuleId; }
    public Long serviceId() { return serviceId; }
    public String message() { return message; }
    public String severity() { return severity; }
    public LocalDateTime triggeredAt() { return triggeredAt; }
    public LocalDateTime resolvedAt() { return resolvedAt; }
    public Map<String, String> metadata() { return metadata; }
}