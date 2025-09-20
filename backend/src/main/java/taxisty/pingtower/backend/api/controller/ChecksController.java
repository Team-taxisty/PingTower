package taxisty.pingtower.backend.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.CheckCreateDto;
import taxisty.pingtower.backend.api.dto.CheckDto;
import taxisty.pingtower.backend.api.service.CheckService;
import taxisty.pingtower.backend.api.service.IdempotencyService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/checks")
public class ChecksController {
    private final CheckService checkService;
    private final IdempotencyService idempotencyService;

    public ChecksController(CheckService checkService, IdempotencyService idempotencyService) {
        this.checkService = checkService;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit,
            @RequestParam(name = "cursor", required = false) String cursor
    ) {
        int lm = Math.min(Math.max(limit == null ? 50 : limit, 1), 500);
        int offset = 0;
        if (StringUtils.hasText(cursor)) {
            try {
                offset = Integer.parseInt(cursor);
                if (offset < 0) offset = 0;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor");
            }
        }
        List<CheckDto> items = checkService.list(lm, offset);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        int total = checkService.count();
        int nextOffset = offset + items.size();
        if (nextOffset < total) {
            resp.put("next_cursor", String.valueOf(nextOffset));
        } else {
            resp.put("next_cursor", null);
        }
        return resp;
    }

    @PostMapping
    public ResponseEntity<CheckDto> create(
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CheckCreateDto body
    ) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        Optional<UUID> existingId = idempotencyService.get(idempotencyKey);
        if (existingId.isPresent()) {
            return checkService.get(existingId.get())
                    .map(dto -> ResponseEntity.ok(dto))
                    .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
        }

        CheckDto created = checkService.create(body);
        idempotencyService.put(idempotencyKey, created.getId());
        return ResponseEntity.created(URI.create("/checks/" + created.getId()))
                .body(created);
    }
}
