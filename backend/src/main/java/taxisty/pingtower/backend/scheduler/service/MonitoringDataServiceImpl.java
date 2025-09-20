package taxisty.pingtower.backend.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taxisty.pingtower.backend.monitoring.repository.CheckResultRepository;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.monitoring.service.MonitoringService;
import taxisty.pingtower.backend.notifications.service.NotificationService;
import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of MonitoringDataService that bridges the scheduler and storage layers.
 * Handles data operations required by monitoring tasks and provides failure alerting.
 */
@Service
@Transactional
public class MonitoringDataServiceImpl implements MonitoringDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringDataServiceImpl.class);
    
    private final MonitoredServiceRepository serviceRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitoringService monitoringService;
    private final NotificationService notificationService;
    
    public MonitoringDataServiceImpl(MonitoredServiceRepository serviceRepository,
                                    CheckResultRepository checkResultRepository,
                                    MonitoringService monitoringService,
                                    NotificationService notificationService) {
        this.serviceRepository = serviceRepository;
        this.checkResultRepository = checkResultRepository;
        this.monitoringService = monitoringService;
        this.notificationService = notificationService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public MonitoredService getMonitoredService(Long serviceId) {
        logger.debug("Retrieving monitored service: {}", serviceId);
        
        Optional<MonitoredService> service = serviceRepository.findById(serviceId);
        if (service.isEmpty()) {
            logger.warn("Monitored service not found: {}", serviceId);
            return null;
        }
        
        MonitoredService monitoredService = service.get();
        if (!monitoredService.isActive()) {
            logger.debug("Monitored service is not active: {}", serviceId);
            return null;
        }
        
        return monitoredService;
    }
    
    @Override
    @Transactional
    public CheckResult saveCheckResult(CheckResult checkResult) {
        logger.debug("Saving check result for service: {}", checkResult.getServiceId());
        
        try {
            // Save to PostgreSQL first
            CheckResult savedResult = checkResultRepository.save(checkResult);
            
            // Process through monitoring service for ClickHouse storage and alerting
            monitoringService.processCheckResult(savedResult);
            
            logger.debug("Successfully saved check result: {}", savedResult.getId());
            return savedResult;
            
        } catch (Exception e) {
            logger.error("Failed to save check result for service: {}", 
                        checkResult.getServiceId(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void handleFailureAlert(MonitoredService service, CheckResult failedResult) {
        logger.debug("Handling failure alert for service: {} with result: {}", 
                    service.getId(), failedResult.getId());
        
        try {
            // Check if we should trigger an alert based on failure patterns
            if (shouldTriggerAlert(service, failedResult)) {
                Alert alert = createFailureAlert(service, failedResult);
                notificationService.sendAlert(alert);
                logger.info("Failure alert sent for service: {}", service.getId());
            }
            
        } catch (Exception e) {
            logger.error("Failed to handle failure alert for service: {}", 
                        service.getId(), e);
        }
    }
    
    /**
     * Determines if an alert should be triggered based on failure patterns
     */
    private boolean shouldTriggerAlert(MonitoredService service, CheckResult failedResult) {
        // Get recent check results to analyze failure pattern
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        var recentFailures = checkResultRepository.findRecentFailuresByServiceId(
                service.getId(), since);
        
        // Trigger alert if we have 3 consecutive failures in the last 15 minutes
        return recentFailures.size() >= 3;
    }
    
    /**
     * Creates a failure alert for the service
     */
    private Alert createFailureAlert(MonitoredService service, CheckResult failedResult) {
        String message = String.format(
                "Service '%s' is experiencing failures. Last check failed with: %s",
                service.getName(),
                failedResult.getErrorMessage() != null ? 
                    failedResult.getErrorMessage() : "Unknown error"
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("serviceId", service.getId().toString());
        metadata.put("serviceName", service.getName());
        metadata.put("serviceUrl", service.getUrl());
        metadata.put("lastResponseCode", String.valueOf(failedResult.getResponseCode()));
        metadata.put("lastResponseTime", String.valueOf(failedResult.getResponseTimeMs()));
        
        Alert alert = new Alert();
        alert.setServiceId(service.getId());
        alert.setMessage(message);
        alert.setSeverity("HIGH");
        alert.setIsResolved(false);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setMetadata(metadata);
        
        return alert;
    }
}