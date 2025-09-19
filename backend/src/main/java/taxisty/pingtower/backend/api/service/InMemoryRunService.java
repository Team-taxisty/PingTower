package taxisty.pingtower.backend.api.service;

import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.api.dto.RunDto;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class InMemoryRunService implements RunService {
    private final List<RunDto> runs = new CopyOnWriteArrayList<>();

    @Override
    public List<RunDto> list(UUID checkId, Instant from, Instant to, int limit) {
        return runs.stream()
                .filter(r -> checkId == null || checkId.equals(r.getCheckId()))
                // Apply time filters consistently to startedAt
                .filter(r -> from == null || (r.getStartedAt() != null && !r.getStartedAt().isBefore(from)))
                .filter(r -> to == null || (r.getStartedAt() != null && !r.getStartedAt().isAfter(to)))
                // Sort by startedAt desc, nulls last
                .sorted(Comparator
                        .comparing(RunDto::getStartedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Helper to add runs later by scheduler/monitoring
    public void addRun(RunDto run) {
        runs.add(run);
    }
}
