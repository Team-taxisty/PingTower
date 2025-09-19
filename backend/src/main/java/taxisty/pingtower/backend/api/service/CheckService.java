package taxisty.pingtower.backend.api.service;

import taxisty.pingtower.backend.api.dto.CheckCreateDto;
import taxisty.pingtower.backend.api.dto.CheckDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckService {
    CheckDto create(CheckCreateDto req);
    Optional<CheckDto> get(UUID id);
    List<CheckDto> list(int limit, int offset);
    int count();
}

