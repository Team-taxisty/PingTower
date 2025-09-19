package taxisty.pingtower.backend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CheckCreateDto {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    private String type; // HTTP, HTTPS, API_JSON, API_XML

    @NotBlank
    private String target;

    @JsonProperty("interval_sec")
    @Min(1)
    private Integer intervalSec = 60;

    public Integer getIntervalSec() {
        return intervalSec;
    }
    public void setIntervalSec(Integer intervalSec) {
        this.intervalSec = intervalSec;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
