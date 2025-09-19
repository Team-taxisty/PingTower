package taxisty.pingtower.backend.notifications.model;

public enum ChannelType {
    TELEGRAM,
    EMAIL,
    WEBHOOK;

    public static ChannelType fromString(String s) {
        if (s == null) return null;
        return ChannelType.valueOf(s.trim().toUpperCase());
    }
}

