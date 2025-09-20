package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Defines the monitoring schedule for a service using cron-like configuration.
 * Supports flexible intervals from seconds to hours based on service criticality.
 */
@Entity
@Table(name = "check_schedule")
public class CheckSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_id", nullable = false, unique = true)
    private Long serviceId;
    
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;
    
    @Column(name = "interval_seconds")
    private int intervalSeconds = 300; // Default 5 minutes
    
    @Column(name = "is_enabled")
    private boolean isEnabled = true;
    
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";
    
    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public CheckSchedule() {}
    
    public CheckSchedule(Long id, Long serviceId, String cronExpression, 
                        int intervalSeconds, boolean isEnabled, String timezone,
                        LocalDateTime nextRunTime, LocalDateTime createdAt, 
                        LocalDateTime updatedAt) {
        this.id = id;
        this.serviceId = serviceId;
        this.cronExpression = cronExpression;
        this.intervalSeconds = intervalSeconds;
        this.isEnabled = isEnabled;
        this.timezone = timezone;
        this.nextRunTime = nextRunTime;
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
    
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    
    public boolean isEnabled() { return isEnabled; }
    public void setIsEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public LocalDateTime getNextRunTime() { return nextRunTime; }
    public void setNextRunTime(LocalDateTime nextRunTime) { this.nextRunTime = nextRunTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long serviceId() { return serviceId; }
    public String cronExpression() { return cronExpression; }
    public int intervalSeconds() { return intervalSeconds; }
    public String timezone() { return timezone; }
    public LocalDateTime nextRunTime() { return nextRunTime; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}