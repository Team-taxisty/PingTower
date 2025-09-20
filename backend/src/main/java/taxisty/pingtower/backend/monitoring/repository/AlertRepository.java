package taxisty.pingtower.backend.monitoring.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import taxisty.pingtower.backend.storage.model.Alert;

/**
 * Repository for managing alerts
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    /**
     * Find alerts by service ID and time range
     */
    @Query("SELECT a FROM Alert a WHERE a.serviceId = :serviceId AND a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    Page<Alert> findByServiceIdAndTriggeredAtAfter(
            @Param("serviceId") Long serviceId, 
            @Param("since") LocalDateTime since, 
            Pageable pageable);
    
    /**
     * Find alerts by resolved status and time range
     */
    @Query("SELECT a FROM Alert a WHERE a.isResolved = :resolved AND a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    Page<Alert> findByIsResolvedAndTriggeredAtAfter(
            @Param("resolved") Boolean resolved, 
            @Param("since") LocalDateTime since, 
            Pageable pageable);
    
    /**
     * Find alerts by time range
     */
    @Query("SELECT a FROM Alert a WHERE a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    Page<Alert> findByTriggeredAtAfter(
            @Param("since") LocalDateTime since, 
            Pageable pageable);
    
    /**
     * Find alerts by service ID, resolved status and time range
     */
    @Query("SELECT a FROM Alert a WHERE a.serviceId = :serviceId AND a.isResolved = :resolved AND a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    Page<Alert> findByServiceIdAndIsResolvedAndTriggeredAtAfter(
            @Param("serviceId") Long serviceId, 
            @Param("resolved") Boolean resolved, 
            @Param("since") LocalDateTime since, 
            Pageable pageable);
    
    /**
     * Find alerts by service ID, resolved status, severity and time range
     */
    @Query("SELECT a FROM Alert a WHERE a.serviceId = :serviceId AND a.isResolved = :resolved AND a.severity = :severity AND a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    Page<Alert> findByServiceIdAndIsResolvedAndSeverityAndTriggeredAtAfter(
            @Param("serviceId") Long serviceId, 
            @Param("resolved") Boolean resolved, 
            @Param("severity") String severity, 
            @Param("since") LocalDateTime since, 
            Pageable pageable);
    
    /**
     * Find unresolved alerts for a service
     */
    @Query("SELECT a FROM Alert a WHERE a.serviceId = :serviceId AND a.isResolved = false ORDER BY a.triggeredAt DESC")
    java.util.List<Alert> findUnresolvedByServiceId(@Param("serviceId") Long serviceId);
    
    /**
     * Count unresolved alerts
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.isResolved = false")
    Long countUnresolved();
}