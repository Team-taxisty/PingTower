package taxisty.pingtower.backend.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for managing monitored services.
 * Handles CRUD operations and service configuration management.
 */
@Repository
public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {
    
    /**
     * Find all active monitored services
     */
    @Query("SELECT ms FROM MonitoredService ms WHERE ms.isActive = true")
    List<MonitoredService> findAllActive();
    
    /**
     * Find monitored services by user ID
     */
    @Query("SELECT ms FROM MonitoredService ms WHERE ms.userId = :userId AND ms.isActive = true")
    List<MonitoredService> findActiveByUserId(@Param("userId") Long userId);
    
    /**
     * Find service by URL to prevent duplicates
     */
    Optional<MonitoredService> findByUrlAndUserId(String url, Long userId);
    
    /**
     * Find services that need checking (for scheduler integration)
     */
    @Query("SELECT ms FROM MonitoredService ms WHERE ms.isActive = true AND ms.updatedAt < :lastCheckTime")
    List<MonitoredService> findServicesForChecking(@Param("lastCheckTime") LocalDateTime lastCheckTime);
    
    /**
     * Count active services by user
     */
    @Query("SELECT COUNT(ms) FROM MonitoredService ms WHERE ms.userId = :userId AND ms.isActive = true")
    Long countActiveByUserId(@Param("userId") Long userId);
    
    /**
     * Find services by name pattern (for search functionality)
     */
    @Query("SELECT ms FROM MonitoredService ms WHERE ms.isActive = true AND LOWER(ms.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MonitoredService> findByNameContainingIgnoreCase(@Param("name") String name);
}