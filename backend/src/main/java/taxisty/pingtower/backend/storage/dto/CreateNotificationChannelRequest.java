package taxisty.pingtower.backend.storage.dto;

/**
 * Data transfer object for setting up notification channels.
 * Configures delivery methods for alert notifications.
 */
public record CreateNotificationChannelRequest(
        String type,
        String name,
        String configuration,
        boolean isDefault
) {}