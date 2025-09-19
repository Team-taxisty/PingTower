package taxisty.pingtower.backend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public class RunDto {
    private UUID id;

    @JsonProperty("check_id")
    private UUID checkId;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("finished_at")
    private Instant finishedAt;

    @JsonProperty("latency_ms")
    private Integer latencyMs;

    private String status; // UP, DOWN, DEGRADED

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCheckId() { return checkId; }
    public void setCheckId(UUID checkId) { this.checkId = checkId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

