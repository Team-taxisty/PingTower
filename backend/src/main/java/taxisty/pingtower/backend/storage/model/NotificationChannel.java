package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Defines notification channels and delivery configurations for alerts.
 * Supports multiple delivery methods including email, Telegram, and webhooks.
 */
@Entity
@Table(name = "notification_channel")
public class NotificationChannel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "configuration", nullable = false, columnDefinition = "TEXT")
    private String configuration;
    
    @Column(name = "is_enabled")
    private boolean isEnabled = true;
    
    @Column(name = "is_default")
    private boolean isDefault = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public NotificationChannel() {}
    
    public NotificationChannel(Long id, Long userId, String type, String name, 
                              String configuration, boolean isEnabled, boolean isDefault,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.configuration = configuration;
        this.isEnabled = isEnabled;
        this.isDefault = isDefault;
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
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    
    public boolean isEnabled() { return isEnabled; }
    public void setIsEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }
    
    public boolean isDefault() { return isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long userId() { return userId; }
    public String type() { return type; }
    public String name() { return name; }
    public String configuration() { return configuration; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}