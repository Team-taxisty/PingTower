package taxisty.pingtower.backend.api.dto;

/**
 * DTO for authentication response
 */
public record AuthResponse(
        String token,
        String type,
        Long id,
        String username,
        String email,
        java.util.Set<String> roles
) {
    public AuthResponse(String token, Long id, String username, String email, java.util.Set<String> roles) {
        this(token, "Bearer", id, username, email, roles);
    }
}
