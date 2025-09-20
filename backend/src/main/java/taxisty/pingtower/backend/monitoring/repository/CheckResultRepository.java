package taxisty.pingtower.backend.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import taxisty.pingtower.backend.storage.model.CheckResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for check results.
 * Handles immediate check result storage and recent data queries.
 */
@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    
    /**
     * Find recent check results for a service (last 24 hours by default)
     */
    @Query("SELECT cr FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.checkTime >= :since ORDER BY cr.checkTime DESC")
    List<CheckResult> findRecentByServiceId(@Param("serviceId") Long serviceId, @Param("since") LocalDateTime since);
    
    /**
     * Find latest check result for a service (single item)
     */
    Optional<CheckResult> findFirstByServiceIdOrderByCheckTimeDesc(Long serviceId);
    
    /**
     * Find check results within time range for a service
     */
    @Query("SELECT cr FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.checkTime BETWEEN :start AND :end ORDER BY cr.checkTime DESC")
    List<CheckResult> findByServiceIdAndTimeRange(@Param("serviceId") Long serviceId, 
                                                  @Param("start") LocalDateTime start, 
                                                  @Param("end") LocalDateTime end);
    
    /**
     * Find failed check results for alerting
     */
    @Query("SELECT cr FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.isSuccessful = false AND cr.checkTime >= :since ORDER BY cr.checkTime DESC")
    List<CheckResult> findRecentFailuresByServiceId(@Param("serviceId") Long serviceId, @Param("since") LocalDateTime since);
    
    /**
     * Count successful checks in time period
     */
    @Query("SELECT COUNT(cr) FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.isSuccessful = true AND cr.checkTime BETWEEN :start AND :end")
    Long countSuccessfulChecks(@Param("serviceId") Long serviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Count total checks in time period
     */
    @Query("SELECT COUNT(cr) FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.checkTime BETWEEN :start AND :end")
    Long countTotalChecks(@Param("serviceId") Long serviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get average response time for successful checks
     */
    @Query("SELECT AVG(cr.responseTimeMs) FROM CheckResult cr WHERE cr.serviceId = :serviceId AND cr.isSuccessful = true AND cr.checkTime BETWEEN :start AND :end")
    Double getAverageResponseTime(@Param("serviceId") Long serviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Find check results older than specified date for cleanup
     */
    @Query("SELECT cr FROM CheckResult cr WHERE cr.checkTime < :cutoffDate")
    List<CheckResult> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete check results older than specified date
     */
    void deleteByCheckTimeBefore(LocalDateTime cutoffDate);
    
    /**
     * Find all unique service IDs that have check results
     */
    @Query("SELECT DISTINCT cr.serviceId FROM CheckResult cr")
    List<Long> findAllServiceIds();
}
