package taxisty.pingtower.backend.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
    private static class Entry {
        final UUID resultId;
        final long createdAtMillis;
        Entry(UUID resultId, long createdAtMillis) {
            this.resultId = resultId;
            this.createdAtMillis = createdAtMillis;
        }
    }

    private final ConcurrentHashMap<String, Entry> keyToEntry = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public IdempotencyService(@Value("${idempotency.ttl-seconds:86400}") long ttlSeconds) {
        this.ttlMillis = ttlSeconds * 1000L; // default 24h
    }

    public Optional<UUID> get(String key) {
        Entry e = keyToEntry.get(key);
        if (e == null) return Optional.empty();
        if (isExpired(e)) {
            keyToEntry.remove(key, e);
            return Optional.empty();
        }
        return Optional.of(e.resultId);
    }

    public void put(String key, UUID id) {
        long now = System.currentTimeMillis();
        keyToEntry.compute(key, (k, existing) -> {
            if (existing == null || isExpired(existing)) {
                return new Entry(id, now);
            }
            return existing;
        });
    }

    private boolean isExpired(Entry e) {
        return ttlMillis > 0 && (System.currentTimeMillis() - e.createdAtMillis) > ttlMillis;
    }

    @Scheduled(fixedDelayString = "${idempotency.cleanup-interval-ms:600000}") 
    public void cleanup() {
        if (ttlMillis <= 0) return;
        long now = System.currentTimeMillis();
        keyToEntry.entrySet().removeIf(en -> (now - en.getValue().createdAtMillis) > ttlMillis);
    }
}
