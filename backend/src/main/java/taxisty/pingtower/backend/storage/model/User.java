package taxisty.pingtower.backend.storage.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents system users with role-based access control.
 * Manages authentication and authorization for the monitoring platform.
 */
public record User(
        Long id,
        String username,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}