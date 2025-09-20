package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a web service or resource being monitored by PingTower.
 * Contains configuration for monitoring checks including URL, intervals, and validation rules.
 */
@Entity
@Table(name = "monitored_service")
public class MonitoredService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "url", nullable = false, length = 2000)
    private String url;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod = "GET";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "json")
    private Map<String, String> headers = new HashMap<>();
    
    @Column(name = "expected_response_code", length = 10)
    private String expectedResponseCode = "200";
    
    @Column(name = "expected_content", columnDefinition = "TEXT")
    private String expectedContent;
    
    @Column(name = "ssl_certificate_check")
    private boolean sslCertificateCheck = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    // Additional fields for enhanced monitoring configuration
    @Column(name = "type", length = 50)
    private String type = "HTTP";
    
    @Column(name = "check_interval_minutes")
    private Integer checkIntervalMinutes = 5;
    
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;
    
    @Column(name = "expected_status_code")
    private Integer expectedStatusCode = 200;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public MonitoredService() {}
    
    // Constructor with parameters
    public MonitoredService(Long id, String name, String description, String url, 
                           String httpMethod, Map<String, String> headers, 
                           String expectedResponseCode, String expectedContent, 
                           boolean sslCertificateCheck, boolean isActive, 
                           Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.httpMethod = httpMethod;
        this.headers = headers != null ? headers : new HashMap<>();
        this.expectedResponseCode = expectedResponseCode;
        this.expectedContent = expectedContent;
        this.sslCertificateCheck = sslCertificateCheck;
        this.isActive = isActive;
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
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public String getExpectedResponseCode() { return expectedResponseCode; }
    public void setExpectedResponseCode(String expectedResponseCode) { this.expectedResponseCode = expectedResponseCode; }
    
    public String getExpectedContent() { return expectedContent; }
    public void setExpectedContent(String expectedContent) { this.expectedContent = expectedContent; }
    
    public boolean isSslCertificateCheck() { return sslCertificateCheck; }
    public void setSslCertificateCheck(boolean sslCertificateCheck) { this.sslCertificateCheck = sslCertificateCheck; }
    
    public boolean isActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    
    // Adapter methods for controller compatibility
    public Boolean getEnabled() { return isActive; }
    public void setEnabled(Boolean enabled) { this.isActive = enabled != null ? enabled : true; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Integer getCheckIntervalMinutes() { return checkIntervalMinutes; }
    public void setCheckIntervalMinutes(Integer checkIntervalMinutes) { this.checkIntervalMinutes = checkIntervalMinutes; }
    
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    public Integer getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(Integer expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Record-like accessor methods for backward compatibility
    public Long id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public String url() { return url; }
    public String httpMethod() { return httpMethod; }
    public Map<String, String> headers() { return headers; }
    public String expectedResponseCode() { return expectedResponseCode; }
    public String expectedContent() { return expectedContent; }
    public boolean sslCertificateCheck() { return sslCertificateCheck; }
    public Long userId() { return userId; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}