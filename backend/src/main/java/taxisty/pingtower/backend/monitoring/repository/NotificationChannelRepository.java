package taxisty.pingtower.backend.monitoring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import taxisty.pingtower.backend.storage.model.NotificationChannel;

/**
 * Repository for managing notification channels
 */
@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    
    /**
     * Find all enabled notification channels
     */
    @Query("SELECT nc FROM NotificationChannel nc WHERE nc.isEnabled = true")
    List<NotificationChannel> findAllEnabled();
    
    /**
     * Find notification channels by type
     */
    @Query("SELECT nc FROM NotificationChannel nc WHERE nc.type = :type AND nc.isEnabled = true")
    List<NotificationChannel> findByTypeAndEnabled(@Param("type") String type);
    
    /**
     * Find notification channel by name
     */
    Optional<NotificationChannel> findByName(String name);
    
    /**
     * Check if a channel with the given name exists (for validation)
     */
    boolean existsByName(String name);
    
    /**
     * Find channels by type
     */
    List<NotificationChannel> findByType(String type);
}