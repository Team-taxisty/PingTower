package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Defines alert rules and conditions for triggering notifications.
 * Supports threshold-based alerts and escalation configurations.
 */
@Entity
@Table(name = "alert_rule")
public class AlertRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "failure_threshold")
    private int failureThreshold = 3;
    
    @Column(name = "warning_threshold")
    private int warningThreshold = 1;
    
    @Column(name = "response_time_threshold_ms")
    private long responseTimeThresholdMs = 5000;
    
    @Column(name = "is_enabled")
    private boolean isEnabled = true;
    
    @Column(name = "severity", length = 50)
    private String severity = "HIGH";
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public AlertRule() {}
    
    public AlertRule(Long id, Long serviceId, String name, String description, 
                    int failureThreshold, int warningThreshold, 
                    long responseTimeThresholdMs, boolean isEnabled, 
                    String severity, Long userId, LocalDateTime createdAt, 
                    LocalDateTime updatedAt) {
        this.id = id;
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.failureThreshold = failureThreshold;
        this.warningThreshold = warningThreshold;
        this.responseTimeThresholdMs = responseTimeThresholdMs;
        this.isEnabled = isEnabled;
        this.severity = severity;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }
    
    public int getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(int warningThreshold) { this.warningThreshold = warningThreshold; }
    
    public long getResponseTimeThresholdMs() { return responseTimeThresholdMs; }
    public void setResponseTimeThresholdMs(long responseTimeThresholdMs) { this.responseTimeThresholdMs = responseTimeThresholdMs; }
    
    public boolean isEnabled() { return isEnabled; }
    public void setIsEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long serviceId() { return serviceId; }
    public String name() { return name; }
    public String description() { return description; }
    public int failureThreshold() { return failureThreshold; }
    public int warningThreshold() { return warningThreshold; }
    public long responseTimeThresholdMs() { return responseTimeThresholdMs; }
    public String severity() { return severity; }
    public Long userId() { return userId; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}