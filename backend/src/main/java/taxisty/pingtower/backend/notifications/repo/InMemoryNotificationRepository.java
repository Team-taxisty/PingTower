package taxisty.pingtower.backend.notifications.repo;

import org.springframework.stereotype.Repository;
import taxisty.pingtower.backend.storage.model.NotificationChannel;
import taxisty.pingtower.backend.storage.model.NotificationDelivery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryNotificationRepository {
    private final AtomicLong idSeq = new AtomicLong(1);
    private final Map<Long, NotificationChannel> channels = new ConcurrentHashMap<>();
    private final List<NotificationDelivery> deliveries = Collections.synchronizedList(new ArrayList<>());

    public NotificationChannel createChannel(Long userId, String type, String name, String configuration, boolean enabled, boolean isDefault) {
        long id = idSeq.getAndIncrement();
        NotificationChannel ch = new NotificationChannel(
                id,
                userId,
                type,
                name,
                configuration,
                enabled,
                isDefault,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        channels.put(id, ch);
        return ch;
    }

    public List<NotificationChannel> listChannels() {
        return new ArrayList<>(channels.values());
    }

    public NotificationChannel getChannel(Long id) {
        return channels.get(id);
    }

    public void addDelivery(NotificationDelivery d) {
        deliveries.add(d);
    }

    public List<NotificationDelivery> listDeliveries() {
        return new ArrayList<>(deliveries);
    }
}

