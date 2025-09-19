package taxisty.pingtower.backend.api.service;

import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.api.dto.CheckCreateDto;
import taxisty.pingtower.backend.api.dto.CheckDto;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class InMemoryCheckService implements CheckService {
    private final Map<UUID, CheckDto> storage = new ConcurrentHashMap<>();
    private final List<UUID> order = Collections.synchronizedList(new ArrayList<>());

    private static final Set<String> ALLOWED_TYPES = Set.of("HTTP", "HTTPS", "API_JSON", "API_XML");

    @Override
    public CheckDto create(CheckCreateDto req) {
        if (!ALLOWED_TYPES.contains(req.getType())) {
            throw new IllegalArgumentException("Unsupported check type: " + req.getType());
        }
        UUID id = UUID.randomUUID();
        CheckDto dto = new CheckDto();
        dto.setId(id);
        dto.setName(req.getName());
        dto.setType(req.getType());
        dto.setTarget(req.getTarget());
        dto.setIntervalSec(req.getIntervalSec() == null ? 60 : req.getIntervalSec());
        dto.setEnabled(true);
        dto.setCreatedAt(Instant.now());
        storage.put(id, dto);
        order.add(id);
        return dto;
    }

    @Override
    public Optional<CheckDto> get(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CheckDto> list(int limit, int offset) {
        synchronized (order) {
            return order.stream()
                    .skip(offset)
                    .limit(limit)
                    .map(storage::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public int count() {
        return storage.size();
    }
}

