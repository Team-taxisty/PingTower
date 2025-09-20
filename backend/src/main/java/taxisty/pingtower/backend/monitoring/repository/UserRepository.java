package taxisty.pingtower.backend.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taxisty.pingtower.backend.storage.model.User;

import java.util.Optional;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find active users
     */
    java.util.List<User> findByIsActiveTrue();
}