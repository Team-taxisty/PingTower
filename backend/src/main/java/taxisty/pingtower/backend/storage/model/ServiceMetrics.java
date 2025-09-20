package taxisty.pingtower.backend.storage.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Aggregated metrics for service availability and performance reporting.
 * Used for dashboard visualization and SLA reporting.
 */
@Entity
@Table(name = "service_metrics", indexes = {
        @Index(name = "idx_service_metrics_service_id", columnList = "service_id"),
        @Index(name = "idx_service_metrics_period", columnList = "period_start, period_end")
})
public class ServiceMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;
    
    @Column(name = "uptime_percentage")
    private double uptimePercentage;
    
    @Column(name = "average_response_time_ms")
    private long averageResponseTimeMs;
    
    @Column(name = "max_response_time_ms")
    private long maxResponseTimeMs;
    
    @Column(name = "min_response_time_ms")
    private long minResponseTimeMs;
    
    @Column(name = "total_checks")
    private int totalChecks;
    
    @Column(name = "successful_checks")
    private int successfulChecks;
    
    @Column(name = "failed_checks")
    private int failedChecks;
    
    @Column(name = "aggregation_period", length = 50)
    private String aggregationPeriod;
    
    public ServiceMetrics() {}
    
    public ServiceMetrics(Long id, Long serviceId, LocalDateTime periodStart, 
                         LocalDateTime periodEnd, double uptimePercentage, 
                         long averageResponseTimeMs, long maxResponseTimeMs, 
                         long minResponseTimeMs, int totalChecks, 
                         int successfulChecks, int failedChecks, 
                         String aggregationPeriod) {
        this.id = id;
        this.serviceId = serviceId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.uptimePercentage = uptimePercentage;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.maxResponseTimeMs = maxResponseTimeMs;
        this.minResponseTimeMs = minResponseTimeMs;
        this.totalChecks = totalChecks;
        this.successfulChecks = successfulChecks;
        this.failedChecks = failedChecks;
        this.aggregationPeriod = aggregationPeriod;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    
    public double getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
    
    public long getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(long averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }
    
    public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
    public void setMaxResponseTimeMs(long maxResponseTimeMs) { this.maxResponseTimeMs = maxResponseTimeMs; }
    
    public long getMinResponseTimeMs() { return minResponseTimeMs; }
    public void setMinResponseTimeMs(long minResponseTimeMs) { this.minResponseTimeMs = minResponseTimeMs; }
    
    public int getTotalChecks() { return totalChecks; }
    public void setTotalChecks(int totalChecks) { this.totalChecks = totalChecks; }
    
    public int getSuccessfulChecks() { return successfulChecks; }
    public void setSuccessfulChecks(int successfulChecks) { this.successfulChecks = successfulChecks; }
    
    public int getFailedChecks() { return failedChecks; }
    public void setFailedChecks(int failedChecks) { this.failedChecks = failedChecks; }
    
    public String getAggregationPeriod() { return aggregationPeriod; }
    public void setAggregationPeriod(String aggregationPeriod) { this.aggregationPeriod = aggregationPeriod; }
    
    // Record-like accessor methods
    public Long id() { return id; }
    public Long serviceId() { return serviceId; }
    public LocalDateTime periodStart() { return periodStart; }
    public LocalDateTime periodEnd() { return periodEnd; }
    public double uptimePercentage() { return uptimePercentage; }
    public long averageResponseTimeMs() { return averageResponseTimeMs; }
    public long maxResponseTimeMs() { return maxResponseTimeMs; }
    public long minResponseTimeMs() { return minResponseTimeMs; }
    public int totalChecks() { return totalChecks; }
    public int successfulChecks() { return successfulChecks; }
    public int failedChecks() { return failedChecks; }
    public String aggregationPeriod() { return aggregationPeriod; }
}