package taxisty.pingtower.backend.notifications.sender;

import taxisty.pingtower.backend.notifications.NotificationMessage;

public interface NotificationSender<C> {
    void send(NotificationMessage message, C config) throws Exception;
}

