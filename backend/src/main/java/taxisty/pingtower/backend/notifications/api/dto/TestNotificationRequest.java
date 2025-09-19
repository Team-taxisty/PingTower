package taxisty.pingtower.backend.notifications.api.dto;

import jakarta.validation.constraints.NotBlank;

public class TestNotificationRequest {
    @NotBlank
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

