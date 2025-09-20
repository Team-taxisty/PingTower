package taxisty.pingtower.backend.scheduler.task;

/**
 * Enumeration of supported task execution types.
 */
public enum TaskType {
    
    PING("PING", "Simple ping check for URL availability"),
    API("API", "API endpoint validation with response body and status code checking"),
    HTTP_CHECK("HTTP_CHECK", "Standard HTTP/HTTPS availability check"),
    API_CHECK("API_CHECK", "API endpoint validation with response parsing"),
    SSL_CHECK("SSL_CHECK", "SSL certificate validation check"),
    HEALTH_CHECK("HEALTH_CHECK", "Application health endpoint check");
    
    private final String code;
    private final String description;
    
    TaskType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskType fromCode(String code) {
        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task type: " + code);
    }
}