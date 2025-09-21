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
        java.util.Set<String> roles,
        String telegramLink,
        String telegramToken,
        Boolean telegramLinked,
        String telegramExpiresAt,
        String telegramBotUsername
) {
    public AuthResponse(String token, Long id, String username, String email, java.util.Set<String> roles) {
        this(token, "Bearer", id, username, email, roles, null, null, null, null, null);
    }

    public AuthResponse(String token,
                        Long id,
                        String username,
                        String email,
                        java.util.Set<String> roles,
                        String telegramLink,
                        String telegramToken,
                        Boolean telegramLinked,
                        String telegramExpiresAt,
                        String telegramBotUsername) {
        this(token, "Bearer", id, username, email, roles, telegramLink, telegramToken, telegramLinked, telegramExpiresAt, telegramBotUsername);
    }
}
