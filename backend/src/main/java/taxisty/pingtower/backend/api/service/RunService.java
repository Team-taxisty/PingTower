package taxisty.pingtower.backend.api.service;

import taxisty.pingtower.backend.api.dto.RunDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RunService {
    List<RunDto> list(UUID checkId, Instant from, Instant to, int limit);
}

