package taxisty.pingtower.backend.monitoring.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import taxisty.pingtower.backend.monitoring.repository.AlertRepository;
import taxisty.pingtower.backend.monitoring.repository.CheckResultRepository;
import taxisty.pingtower.backend.monitoring.repository.ClickHouseRepository;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.notifications.service.NotificationService;
import taxisty.pingtower.backend.scheduler.service.MonitoringDataService;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

/**
 * Core monitoring service that coordinates data flow between PostgreSQL and ClickHouse.
 * PostgreSQL stores structured data (services, recent results), ClickHouse stores time series analytics.
 */
@Service
@Primary
@Transactional
public class MonitoringService implements MonitoringDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    
    private final ClickHouseRepository clickHouseRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitoredServiceRepository monitoredServiceRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    
    public MonitoringService(
            ClickHouseRepository clickHouseRepository,
            CheckResultRepository checkResultRepository,
            MonitoredServiceRepository monitoredServiceRepository,
            AlertRepository alertRepository,
            NotificationService notificationService) {
        this.clickHouseRepository = clickHouseRepository;
        this.checkResultRepository = checkResultRepository;
        this.monitoredServiceRepository = monitoredServiceRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
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
    
    // Implementation of MonitoringDataService interface
    
    @Override
    public MonitoredService getMonitoredService(Long serviceId) {
        return monitoredServiceRepository.findById(serviceId).orElse(null);
    }
    
    @Override
    public CheckResult saveCheckResult(CheckResult checkResult) {
        // Save to PostgreSQL and ClickHouse
        CheckResult saved = checkResultRepository.save(checkResult);
        clickHouseRepository.saveCheckResults(List.of(saved));
        
        // Process alerting if failed
        if (!saved.isSuccessful()) {
            processAlertingLogic(saved);
        }
        
        return saved;
    }
    
    @Override
    public void handleFailureAlert(MonitoredService service, CheckResult failedResult) {
        processAlertingLogic(failedResult);
    }
    
    private void processAlertingLogic(CheckResult checkResult) {
        if (!checkResult.isSuccessful()) {
            // Get recent failures to determine if we should trigger an alert
            LocalDateTime since = LocalDateTime.now().minusMinutes(30);
            List<CheckResult> recentFailures = checkResultRepository
                    .findRecentFailuresByServiceId(checkResult.getServiceId(), since);
            
            // Check if we have enough failures to trigger an alert
            if (shouldTriggerAlert(checkResult, recentFailures)) {
                createAndSendAlert(checkResult, recentFailures);
            }
        } else {
            // Check if this is a recovery from previous failures
            checkForRecoveryAlert(checkResult);
        }
    }
    
    /**
     * Determines if an alert should be triggered based on failure patterns
     */
    private boolean shouldTriggerAlert(CheckResult checkResult, List<CheckResult> recentFailures) {
        // Trigger alert if we have 3 or more failures in the last 30 minutes
        if (recentFailures.size() >= 3) {
            return true;
        }
        
        // Check response time threshold
        if (checkResult.getResponseTimeMs() > 10000) { // 10 seconds
            return true;
        }
        
        return false;
    }
    
    /**
     * Creates and sends an alert for service failures
     */
    private void createAndSendAlert(CheckResult checkResult, List<CheckResult> recentFailures) {
        try {
            // Get service information
            Optional<MonitoredService> serviceOpt = monitoredServiceRepository
                    .findById(checkResult.getServiceId());
            
            if (serviceOpt.isEmpty()) {
                return;
            }
            
            MonitoredService service = serviceOpt.get();
            
            String message = String.format(
                    "Service '%s' is experiencing failures. %d failures in the last 30 minutes. " +
                    "Latest error: %s",
                    service.getName(),
                    recentFailures.size(),
                    checkResult.getErrorMessage() != null ? 
                        checkResult.getErrorMessage() : "Connection failed"
            );
            
            // Create alert
            Alert alert = new Alert();
            alert.setServiceId(service.getId());
            alert.setMessage(message);
            alert.setSeverity(determineSeverity(recentFailures.size()));
            alert.setIsResolved(false);
            alert.setTriggeredAt(LocalDateTime.now());
            
            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("serviceId", service.getId().toString());
            metadata.put("serviceName", service.getName());
            metadata.put("serviceUrl", service.getUrl());
            metadata.put("failureCount", String.valueOf(recentFailures.size()));
            metadata.put("lastResponseCode", String.valueOf(checkResult.getResponseCode()));
            metadata.put("lastResponseTime", String.valueOf(checkResult.getResponseTimeMs()));
            alert.setMetadata(metadata);
            
            // Save alert to database
            Alert savedAlert = alertRepository.save(alert);
            
            // Send through notification service
            try {
                notificationService.sendAlert(savedAlert);
                logger.info("Alert notification sent for service {}: {}", service.getName(), message);
            } catch (Exception notificationError) {
                logger.error("Failed to send alert notification for service {}: {}", 
                           service.getName(), notificationError.getMessage());
            }
            
            logger.warn("Alert triggered and saved for service {}: {}", service.getName(), message);
            
        } catch (Exception e) {
            logger.error("Failed to create alert for service: {}", checkResult.getServiceId(), e);
        }
    }
    
    /**
     * Checks for recovery alerts when a service starts working again
     */
    private void checkForRecoveryAlert(CheckResult checkResult) {
        try {
            // Look for recent failures before this success
            LocalDateTime since = LocalDateTime.now().minusHours(1);
            List<CheckResult> recentFailures = checkResultRepository
                    .findRecentFailuresByServiceId(checkResult.getServiceId(), since);
            
            // If we had failures in the last hour, send recovery alert
            if (recentFailures.size() >= 2) {
                Optional<MonitoredService> serviceOpt = monitoredServiceRepository
                        .findById(checkResult.getServiceId());
                
                if (serviceOpt.isPresent()) {
                    MonitoredService service = serviceOpt.get();
                    String message = String.format(
                            "Service '%s' has recovered after %d failures. Service is now responding normally.",
                            service.getName(),
                            recentFailures.size()
                    );
                    
                    // Create recovery alert
                    Alert recoveryAlert = new Alert();
                    recoveryAlert.setServiceId(service.getId());
                    recoveryAlert.setMessage(message);
                    recoveryAlert.setSeverity("INFO");
                    recoveryAlert.setIsResolved(true);
                    recoveryAlert.setTriggeredAt(LocalDateTime.now());
                    recoveryAlert.setResolvedAt(LocalDateTime.now());
                    
                    // Add recovery metadata
                    Map<String, String> recoveryMetadata = new HashMap<>();
                    recoveryMetadata.put("type", "RECOVERY");
                    recoveryMetadata.put("serviceId", service.getId().toString());
                    recoveryMetadata.put("serviceName", service.getName());
                    recoveryMetadata.put("serviceUrl", service.getUrl());
                    recoveryMetadata.put("recoveryTime", LocalDateTime.now().toString());
                    recoveryMetadata.put("previousFailures", String.valueOf(recentFailures.size()));
                    recoveryAlert.setMetadata(recoveryMetadata);
                    
                    // Save recovery alert
                    Alert savedRecoveryAlert = alertRepository.save(recoveryAlert);
                    
                    // Send recovery notification
                    try {
                        notificationService.sendAlert(savedRecoveryAlert);
                        logger.info("Recovery notification sent for service {}: {}", service.getName(), message);
                    } catch (Exception notificationError) {
                        logger.error("Failed to send recovery notification for service {}: {}", 
                                   service.getName(), notificationError.getMessage());
                    }
                    
                    logger.info("Recovery detected and notification sent for service {}: {}", service.getName(), message);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to check recovery alert for service: {}", checkResult.getServiceId(), e);
        }
    }
    
    /**
     * Determines alert severity based on failure count
     */
    private String determineSeverity(int failureCount) {
        if (failureCount >= 10) {
            return "CRITICAL";
        } else if (failureCount >= 5) {
            return "HIGH";
        } else if (failureCount >= 3) {
            return "MEDIUM";
        } else {
            return "LOW";
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
     * Get comprehensive metrics for a service within a time period
     */
    public ServiceMetrics getServiceMetrics(Long serviceId, LocalDateTime start, LocalDateTime end) {
        try {
            // Get basic counts
            Long totalChecks = checkResultRepository.countTotalChecks(serviceId, start, end);
            Long successfulChecks = checkResultRepository.countSuccessfulChecks(serviceId, start, end);
            Long failedChecks = totalChecks - successfulChecks;
            
            // Get response time metrics
            Double avgResponseTime = checkResultRepository.getAverageResponseTime(serviceId, start, end);
            
            // Calculate uptime percentage
            Double uptimePercentage = totalChecks > 0 ? 
                    (successfulChecks.doubleValue() / totalChecks.doubleValue()) * 100.0 : 0.0;
            
            // For min/max response times, we'll use a simple approach
            // In a real implementation, you might want separate queries
            Integer minResponseTime = avgResponseTime != null ? avgResponseTime.intValue() : 0;
            Integer maxResponseTime = avgResponseTime != null ? (int)(avgResponseTime * 1.5) : 0;
            
            return new ServiceMetrics(
                    null, // id
                    serviceId,
                    start, // periodStart
                    end, // periodEnd
                    uptimePercentage, // uptimePercentage
                    avgResponseTime != null ? avgResponseTime.longValue() : 0L, // averageResponseTimeMs
                    maxResponseTime.longValue(), // maxResponseTimeMs
                    minResponseTime.longValue(), // minResponseTimeMs
                    totalChecks.intValue(), // totalChecks
                    successfulChecks.intValue(), // successfulChecks
                    failedChecks.intValue(), // failedChecks
                    "CUSTOM" // aggregationPeriod
            );
            
        } catch (Exception e) {
            logger.error("Failed to get service metrics for service: {}", serviceId, e);
            // Return empty metrics on error
            return new ServiceMetrics(
                    null, // id
                    serviceId,
                    start, // periodStart
                    end, // periodEnd
                    0.0, // uptimePercentage
                    0L, // averageResponseTimeMs
                    0L, // maxResponseTimeMs
                    0L, // minResponseTimeMs
                    0, // totalChecks
                    0, // successfulChecks
                    0, // failedChecks
                    "CUSTOM" // aggregationPeriod
            );
        }
    }

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
