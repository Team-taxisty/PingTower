package taxisty.pingtower.backend.notifications;

public enum NotificationChannelType {
    TELEGRAM,
    EMAIL,
    WEBHOOK;

    public static NotificationChannelType from(String value) {
        if (value == null) return null;
        return NotificationChannelType.valueOf(value.trim().toUpperCase());
    }
}

