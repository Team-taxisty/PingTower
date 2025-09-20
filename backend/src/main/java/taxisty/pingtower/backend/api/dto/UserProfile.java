package taxisty.pingtower.backend.api.dto;

/**
 * DTO for user profile response
 */
public record UserProfile(
        Long id,
        String username,
        String email,
        boolean isActive
) {}