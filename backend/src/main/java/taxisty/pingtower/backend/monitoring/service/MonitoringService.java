package taxisty.pingtower.backend.monitoring.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taxisty.pingtower.backend.monitoring.repository.ClickHouseRepository;
import taxisty.pingtower.backend.monitoring.repository.CheckResultRepository;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Core monitoring service that coordinates data flow between PostgreSQL and ClickHouse.
 * PostgreSQL stores structured data (services, recent results), ClickHouse stores time series analytics.
 */
@Service
@Transactional
public class MonitoringService {
    
    private final ClickHouseRepository clickHouseRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitoredServiceRepository monitoredServiceRepository;
    
    public MonitoringService(
            ClickHouseRepository clickHouseRepository,
            CheckResultRepository checkResultRepository,
            MonitoredServiceRepository monitoredServiceRepository) {
        this.clickHouseRepository = clickHouseRepository;
        this.checkResultRepository = checkResultRepository;
        this.monitoredServiceRepository = monitoredServiceRepository;
    }
    
    /**
     * Process and store monitoring check results from scheduler
     */
    public void processCheckResult(CheckResult checkResult) {
        // Store immediately in PostgreSQL for recent data access
        checkResultRepository.save(checkResult);
        
        // Store in ClickHouse for long-term analytics
        clickHouseRepository.saveCheckResults(List.of(checkResult));
        
        // Process alerting logic if check failed
        if (!checkResult.isSuccessful()) {
            processAlertingLogic(checkResult);
        }
    }
    
    /**
     * Process multiple check results in batch
     */
    public void processCheckResults(List<CheckResult> checkResults) {
        if (checkResults.isEmpty()) return;
        
        // Batch save to PostgreSQL for immediate access
        checkResultRepository.saveAll(checkResults);
        
        // Batch insert to ClickHouse for analytics
        clickHouseRepository.saveCheckResults(checkResults);
        
        // Process each result for alerts and notifications
        for (CheckResult result : checkResults) {
            if (!result.isSuccessful()) {
                processAlertingLogic(result);
            }
        }
    }
    
    /**
     * Get monitoring dashboard data for a service
     */
    public MonitoringDashboardData getDashboardData(Long serviceId, LocalDateTime start, LocalDateTime end) {
        Double uptimePercentage = clickHouseRepository.getServiceUptimePercentage(serviceId, start, end);
        Double averageResponseTime = clickHouseRepository.getAverageResponseTime(serviceId, start, end);
        List<CheckResult> recentChecks = clickHouseRepository.getCheckResultsByServiceId(serviceId, start, end);
        List<ServiceMetrics> hourlyMetrics = clickHouseRepository.getHourlyMetrics(serviceId, start, end);
        
        return new MonitoringDashboardData(
            serviceId,
            uptimePercentage != null ? uptimePercentage : 0.0,
            averageResponseTime != null ? averageResponseTime : 0.0,
            recentChecks,
            hourlyMetrics
        );
    }
    
    /**
     * Get service availability status (UP, DOWN, DEGRADED)
     */
    public ServiceStatus getServiceStatus(Long serviceId) {
        // First try PostgreSQL for recent data (faster)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<CheckResult> recentChecks = checkResultRepository.findRecentByServiceId(serviceId, fiveMinutesAgo);
        
        // Fallback to ClickHouse if no recent data in PostgreSQL
        if (recentChecks.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            recentChecks = clickHouseRepository.getCheckResultsByServiceId(serviceId, fiveMinutesAgo, now);
        }
        
        if (recentChecks.isEmpty()) {
            return ServiceStatus.UNKNOWN;
        }
        
        boolean hasSuccessful = recentChecks.stream().anyMatch(CheckResult::isSuccessful);
        boolean hasFailed = recentChecks.stream().anyMatch(result -> !result.isSuccessful());
        
        if (hasSuccessful && !hasFailed) {
            return ServiceStatus.UP;
        } else if (!hasSuccessful && hasFailed) {
            return ServiceStatus.DOWN;
        } else {
            return ServiceStatus.DEGRADED;
        }
    }
    
    /**
     * Get all monitored services for a user
     */
    public List<MonitoredService> getMonitoredServices(Long userId) {
        return monitoredServiceRepository.findActiveByUserId(userId);
    }
    
    /**
     * Get all active monitored services (for scheduler)
     */
    public List<MonitoredService> getAllActiveServices() {
        return monitoredServiceRepository.findAllActive();
    }
    
    /**
     * Get latest check result for a service
     */
    public Optional<CheckResult> getLatestCheckResult(Long serviceId) {
        return checkResultRepository.findFirstByServiceIdOrderByCheckTimeDesc(serviceId)
                .or(() -> {
                    // Fallback to ClickHouse
                    LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
                    LocalDateTime now = LocalDateTime.now();
                    List<CheckResult> results = clickHouseRepository.getCheckResultsByServiceId(serviceId, oneDayAgo, now);
                    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
                });
    }
    
    /**
     * Get recent check results for dashboard
     */
    public List<CheckResult> getRecentCheckResults(Long serviceId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        // Try PostgreSQL first for recent data
        List<CheckResult> results = checkResultRepository.findRecentByServiceId(serviceId, since);
        
        // If not enough data in PostgreSQL, get from ClickHouse
        if (results.size() < 10) {
            LocalDateTime now = LocalDateTime.now();
            results = clickHouseRepository.getCheckResultsByServiceId(serviceId, since, now);
        }
        
        return results;
    }
    
    /**
     * Initialize ClickHouse tables for monitoring data
     */
    public void initializeMonitoringTables() {
        clickHouseRepository.initializeTables();
    }
    
    /**
     * Clean up old monitoring data beyond retention period
     */
    public void cleanupOldMonitoringData(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        clickHouseRepository.cleanupOldData(cutoffDate);
    }
    
    private void processAlertingLogic(CheckResult checkResult) {
        if (!checkResult.isSuccessful()) {
            // TODO: Check alert rules and trigger notifications
            // This will be expanded with alert rule processing
        }
    }
    
    /**
     * Data class for monitoring dashboard information
     */
    public record MonitoringDashboardData(
        Long serviceId,
        Double uptimePercentage,
        Double averageResponseTime,
        List<CheckResult> recentChecks,
        List<ServiceMetrics> hourlyMetrics
    ) {}
    
    /**
     * Enumeration for service availability status
     */
    public enum ServiceStatus {
        UP,       // All checks passing
        DOWN,     // All checks failing
        DEGRADED, // Mixed results
        UNKNOWN   // No recent data
    }
}
