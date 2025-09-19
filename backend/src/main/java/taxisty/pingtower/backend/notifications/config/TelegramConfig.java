package taxisty.pingtower.backend.notifications.config;

public record TelegramConfig(
        String botToken,
        String chatId
) {}

