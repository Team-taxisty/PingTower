package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Contains the results of a service availability check including response metrics.
 * Stores historical data for analytics and reporting purposes.
 */
@Entity
@Table(name = "check_result", indexes = {
        @Index(name = "idx_check_result_service_id", columnList = "service_id"),
        @Index(name = "idx_check_result_check_time", columnList = "check_time")
})
public class CheckResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;
    
    @Column(name = "is_successful", nullable = false)
    private boolean isSuccessful;
    
    @Column(name = "response_code")
    private int responseCode;
    
    @Column(name = "response_time_ms")
    private long responseTimeMs;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "ssl_valid")
    private boolean sslValid;
    
    @Column(name = "ssl_expiry_date")
    private LocalDateTime sslExpiryDate;
    
    @Column(name = "check_location", length = 100)
    private String checkLocation;
    
    // Default constructor
    public CheckResult() {}
    
    // Constructor with all parameters
    public CheckResult(Long id, Long serviceId, LocalDateTime checkTime, 
                      boolean isSuccessful, int responseCode, long responseTimeMs,
                      String responseBody, String errorMessage, boolean sslValid,
                      LocalDateTime sslExpiryDate, String checkLocation) {
        this.id = id;
        this.serviceId = serviceId;
        this.checkTime = checkTime;
        this.isSuccessful = isSuccessful;
        this.responseCode = responseCode;
        this.responseTimeMs = responseTimeMs;
        this.responseBody = responseBody;
        this.errorMessage = errorMessage;
        this.sslValid = sslValid;
        this.sslExpiryDate = sslExpiryDate;
        this.checkLocation = checkLocation;
    }
    
    @PrePersist
    protected void onCreate() {
        if (checkTime == null) {
            checkTime = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public LocalDateTime getCheckTime() { return checkTime; }
    public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
    
    public boolean getIsSuccessful() { return isSuccessful; }
    public void setIsSuccessful(boolean isSuccessful) { this.isSuccessful = isSuccessful; }
    
    public int getResponseCode() { return responseCode; }
    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public boolean isSslValid() { return sslValid; }
    public void setSslValid(boolean sslValid) { this.sslValid = sslValid; }
    
    public LocalDateTime getSslExpiryDate() { return sslExpiryDate; }
    public void setSslExpiryDate(LocalDateTime sslExpiryDate) { this.sslExpiryDate = sslExpiryDate; }
    
    public String getCheckLocation() { return checkLocation; }
    public void setCheckLocation(String checkLocation) { this.checkLocation = checkLocation; }

    // Adapter methods for controller compatibility
    public LocalDateTime getCheckedAt() { return checkTime; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkTime = checkedAt; }

    // Record-like accessor methods for backward compatibility
    public Long id() { return id; }
    public Long serviceId() { return serviceId; }
    public LocalDateTime checkTime() { return checkTime; }
    public boolean isSuccessful() { return isSuccessful; }
    public int responseCode() { return responseCode; }
    public long responseTimeMs() { return responseTimeMs; }
    public String responseBody() { return responseBody; }
    public String errorMessage() { return errorMessage; }
    public boolean sslValid() { return sslValid; }
    public LocalDateTime sslExpiryDate() { return sslExpiryDate; }
    public String checkLocation() { return checkLocation; }
}