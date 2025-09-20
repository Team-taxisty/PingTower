package taxisty.pingtower.backend.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.CheckCreateDto;
import taxisty.pingtower.backend.api.dto.CheckDto;
import taxisty.pingtower.backend.api.service.CheckService;
import taxisty.pingtower.backend.api.service.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/checks")
@Tag(name = "Проверки", description = "API для управления мониторинговыми проверками")
public class ChecksController {
    private final CheckService checkService;
    private final IdempotencyService idempotencyService;

    public ChecksController(CheckService checkService, IdempotencyService idempotencyService) {
        this.checkService = checkService;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @Operation(summary = "Получить список проверок", description = "Возвращает постраничный список мониторинговых проверок")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Проверки успешно получены")
    })
    public Map<String, Object> list(
            @Parameter(description = "Максимальное количество элементов для возврата") @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit,
            @Parameter(description = "Курсор для пагинации") @RequestParam(name = "cursor", required = false) String cursor
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
    @Operation(summary = "Create check", description = "Create a new monitoring check with idempotency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Check created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or missing Idempotency-Key"),
        @ApiResponse(responseCode = "409", description = "Idempotency conflict")
    })
    public ResponseEntity<CheckDto> create(
            @Parameter(description = "Idempotency key for request deduplication") @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
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
