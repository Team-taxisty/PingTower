package taxisty.pingtower.backend.monitoring.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taxisty.pingtower.backend.monitoring.repository.CheckResultRepository;
import taxisty.pingtower.backend.monitoring.repository.ClickHouseRepository;
import taxisty.pingtower.backend.storage.model.CheckResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for synchronizing data between PostgreSQL and ClickHouse.
 * Handles data migration, cleanup, and synchronization tasks.
 */
@Service
@Transactional
public class DataSynchronizationService {
    
    private final CheckResultRepository checkResultRepository;
    private final ClickHouseRepository clickHouseRepository;
    
    public DataSynchronizationService(
            CheckResultRepository checkResultRepository,
            ClickHouseRepository clickHouseRepository) {
        this.checkResultRepository = checkResultRepository;
        this.clickHouseRepository = clickHouseRepository;
    }
    
    /**
     * Migrate old PostgreSQL check results to ClickHouse
     */
    public void migrateOldCheckResults() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // Keep 7 days in PostgreSQL
        List<CheckResult> oldResults = checkResultRepository.findOlderThan(cutoffDate);
        
        if (!oldResults.isEmpty()) {
            // Ensure data exists in ClickHouse
            clickHouseRepository.saveCheckResults(oldResults);
            
            // Remove from PostgreSQL after successful migration
            checkResultRepository.deleteByCheckTimeBefore(cutoffDate);
            
            System.out.println("Migrated " + oldResults.size() + " check results to ClickHouse");
        }
    }
    
    /**
     * Synchronize recent data to ClickHouse
     */
    public void synchronizeRecentData() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Long> serviceIds = checkResultRepository.findAllServiceIds();
        
        for (Long serviceId : serviceIds) {
            List<CheckResult> recentResults = checkResultRepository
                .findByServiceIdAndTimeRange(serviceId, oneDayAgo, LocalDateTime.now());
            
            if (!recentResults.isEmpty()) {
                clickHouseRepository.saveCheckResults(recentResults);
            }
        }
    }
    
    /**
     * Cleanup old data from both databases
     */
    public void cleanupOldData() {
        // Keep 30 days in PostgreSQL for recent access
        LocalDateTime postgresCutoff = LocalDateTime.now().minusDays(30);
        checkResultRepository.deleteByCheckTimeBefore(postgresCutoff);
        
        // Keep 2 years in ClickHouse for analytics (TTL will handle this automatically)
        LocalDateTime clickHouseCutoff = LocalDateTime.now().minusYears(2);
        clickHouseRepository.cleanupOldData(clickHouseCutoff);
        
        System.out.println("Completed data cleanup for both PostgreSQL and ClickHouse");
    }
    
    /**
     * Verify data consistency between databases
     */
    public DataConsistencyReport verifyDataConsistency(Long serviceId, LocalDateTime start, LocalDateTime end) {
        // Get counts from PostgreSQL
        Long postgresTotal = checkResultRepository.countTotalChecks(serviceId, start, end);
        Long postgresSuccessful = checkResultRepository.countSuccessfulChecks(serviceId, start, end);
        
        // Get data from ClickHouse
        List<CheckResult> clickHouseResults = clickHouseRepository.getCheckResultsByServiceId(serviceId, start, end);
        Long clickHouseTotal = (long) clickHouseResults.size();
        Long clickHouseSuccessful = clickHouseResults.stream()
            .mapToLong(r -> r.isSuccessful() ? 1 : 0)
            .sum();
        
        boolean isConsistent = postgresTotal.equals(clickHouseTotal) && 
                              postgresSuccessful.equals(clickHouseSuccessful);
        
        return new DataConsistencyReport(
            serviceId,
            start,
            end,
            postgresTotal,
            postgresSuccessful,
            clickHouseTotal,
            clickHouseSuccessful,
            isConsistent
        );
    }
    
    /**
     * Force synchronization of specific service data
     */
    public void forceSyncServiceData(Long serviceId, LocalDateTime start, LocalDateTime end) {
        List<CheckResult> postgresResults = checkResultRepository
            .findByServiceIdAndTimeRange(serviceId, start, end);
        
        if (!postgresResults.isEmpty()) {
            clickHouseRepository.saveCheckResults(postgresResults);
            System.out.println("Force synced " + postgresResults.size() + 
                             " results for service " + serviceId + " to ClickHouse");
        }
    }
    
    /**
     * Scheduled task to migrate old data (runs daily at 3 AM)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledDataMigration() {
        try {
            migrateOldCheckResults();
        } catch (Exception e) {
            System.err.println("Error during scheduled data migration: " + e.getMessage());
        }
    }
    
    /**
     * Scheduled task to synchronize recent data (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void scheduledDataSync() {
        try {
            synchronizeRecentData();
        } catch (Exception e) {
            System.err.println("Error during scheduled data sync: " + e.getMessage());
        }
    }
    
    /**
     * Scheduled task to cleanup old data (runs weekly on Sunday at 4 AM)
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void scheduledDataCleanup() {
        try {
            cleanupOldData();
        } catch (Exception e) {
            System.err.println("Error during scheduled data cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Data consistency report record
     */
    public record DataConsistencyReport(
        Long serviceId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Long postgresTotal,
        Long postgresSuccessful,
        Long clickHouseTotal,
        Long clickHouseSuccessful,
        Boolean isConsistent
    ) {}
}