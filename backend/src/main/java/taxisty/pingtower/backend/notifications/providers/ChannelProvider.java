package taxisty.pingtower.backend.notifications.providers;

import taxisty.pingtower.backend.storage.model.Alert;
import taxisty.pingtower.backend.storage.model.NotificationChannel;

public interface ChannelProvider {
    String type();

    /**
     * Sends an alert using the provided channel configuration.
     * Returns DeliveryResult indicating success or retry hint.
     */
    DeliveryResult send(Alert alert, NotificationChannel channel);

    record DeliveryResult(boolean success, Integer httpCode, String error, Long retryAfterSeconds) {}
}

