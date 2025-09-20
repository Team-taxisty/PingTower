package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks notification delivery attempts and their status.
 * Maintains audit trail for notification reliability and debugging.
 */
@Entity
@Table(name = "notification_delivery")
public class NotificationDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alert_id", nullable = false)
    private Long alertId;
    
    @Column(name = "channel_id", nullable = false)
    private Long channelId;
    
    @Column(name = "status", length = 50)
    private String status;
    
    @Column(name = "delivery_method", length = 50)
    private String deliveryMethod;
    
    @Column(name = "attempt_count")
    private int attemptCount = 1;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    public NotificationDelivery() {}
    
    public NotificationDelivery(Long id, Long alertId, Long channelId, String status,
                               String deliveryMethod, int attemptCount, String errorMessage,
                               LocalDateTime sentAt, LocalDateTime deliveredAt) {
        this.id = id;
        this.alertId = alertId;
        this.channelId = channelId;
        this.status = status;
        this.deliveryMethod = deliveryMethod;
        this.attemptCount = attemptCount;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
        this.deliveredAt = deliveredAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    
    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long alertId() { return alertId; }
    public Long channelId() { return channelId; }
    public String status() { return status; }
    public String deliveryMethod() { return deliveryMethod; }
    public int attemptCount() { return attemptCount; }
    public String errorMessage() { return errorMessage; }
    public LocalDateTime sentAt() { return sentAt; }
    public LocalDateTime deliveredAt() { return deliveredAt; }
}