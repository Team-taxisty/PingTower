package taxisty.pingtower.backend.api.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taxisty.pingtower.backend.api.dto.RunDto;
import taxisty.pingtower.backend.api.service.RunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/runs")
@Tag(name = "Запуски проверок", description = "API для получения результатов запусков проверок")
public class RunsController {
    private final RunService runService;

    public RunsController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping
    @Operation(summary = "Получить список запусков", description = "Возвращает список результатов запусков проверок с фильтрацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список запусков успешно получен")
    })
    public Map<String, Object> list(
            @Parameter(description = "ID проверки для фильтрации") @RequestParam(name = "check_id", required = false) UUID checkId,
            @Parameter(description = "Начальная дата для фильтрации") @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Конечная дата для фильтрации") @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @Parameter(description = "Максимальное количество результатов") @RequestParam(name = "limit", required = false, defaultValue = "1000") Integer limit
    ) {
        int lm = Math.min(Math.max(limit == null ? 1000 : limit, 1), 10000);
        List<RunDto> items = runService.list(checkId, from, to, lm);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        return resp;
    }
}

