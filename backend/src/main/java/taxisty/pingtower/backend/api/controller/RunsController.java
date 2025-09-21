package taxisty.pingtower.backend.api.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taxisty.pingtower.backend.api.dto.RunDto;
import taxisty.pingtower.backend.api.service.RunService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/runs")
public class RunsController {
    private final RunService runService;

    public RunsController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping
    public Map<String, Object> list(
        @RequestParam(name = "check_id", required = false) UUID checkId,
        @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(name = "limit", required = false, defaultValue = "1000") Integer limit
    ) {
        int lm = Math.min(Math.max(limit == null ? 1000 : limit, 1), 10000);
        List<RunDto> items = runService.list(checkId, from, to, lm);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        return resp;
    }
}

