package taxisty.pingtower.backend.scheduler.service;

import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

/**
 * Interface for data access operations required by the monitoring scheduler.
 * This interface should be implemented by the storage layer to provide
 * necessary data operations for monitoring execution.
 */
public interface MonitoringDataService {
    
    /**
     * Retrieves a monitored service by its ID.
     * 
     * @param serviceId The service ID
     * @return MonitoredService or null if not found
     */
    MonitoredService getMonitoredService(Long serviceId);
    
    /**
     * Saves a check result to storage.
     * 
     * @param checkResult The check result to save
     * @return Saved check result with generated ID
     */
    CheckResult saveCheckResult(CheckResult checkResult);
    
    /**
     * Handles failure alerting when a monitoring check fails.
     * This method should trigger alert processing based on configured rules.
     * 
     * @param service The monitored service
     * @param failedResult The failed check result
     */
    void handleFailureAlert(MonitoredService service, CheckResult failedResult);
}