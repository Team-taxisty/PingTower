package taxisty.pingtower.backend.monitoring.repository;

import java.time.LocalDateTime;
import java.util.List;

import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

/**
 * Repository interface for ClickHouse time series data operations.
 * Handles high-performance analytics and historical data storage.
 */
public interface ClickHouseRepository {
    
    /**
     * Store check results in ClickHouse for analytics
     */
    void saveCheckResults(List<CheckResult> checkResults);
    
    /**
     * Store aggregated service metrics
     */
    void saveServiceMetrics(List<ServiceMetrics> serviceMetrics);
    
    /**
     * Get check results for a service within a time range
     */
    List<CheckResult> getCheckResultsByServiceId(Long serviceId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Get service metrics for analytics and reporting
     */
    List<ServiceMetrics> getServiceMetrics(Long serviceId, LocalDateTime start, LocalDateTime end, String aggregationPeriod);
    
    /**
     * Get aggregated uptime percentage for a service
     */
    Double getServiceUptimePercentage(Long serviceId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Get average response time for a service
     */
    Double getAverageResponseTime(Long serviceId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Get check results grouped by hour for dashboard visualization
     */
    List<ServiceMetrics> getHourlyMetrics(Long serviceId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Initialize ClickHouse tables if they don't exist
     */
    void initializeTables();
    
    /**
     * Clean up old data beyond retention period
     */
    void cleanupOldData(LocalDateTime beforeDate);
}