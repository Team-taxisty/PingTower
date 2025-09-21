package taxisty.pingtower.backend.scheduler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementation of simple ping monitoring checks.
 * Performs basic availability checks for URLs.
 */
@Component
public class PingMonitoringTask implements ScheduledTask {
    
    private static final Logger logger = LoggerFactory.getLogger(PingMonitoringTask.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    @Override
    public CheckResult execute(MonitoredService service) {
    long startTime = System.currentTimeMillis();
    LocalDateTime checkTime = LocalDateTime.now();
    Integer timeoutVal = service.getTimeoutSeconds();
    int configuredTimeout = timeoutVal != null ? timeoutVal : DEFAULT_TIMEOUT_SECONDS;
        
    // Создаем клиент с таймаутом конкретного сервиса (дешево для ping)
    HttpClient dynamicClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(configuredTimeout))
        .build();
        
    try {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(service.url()))
            .timeout(Duration.ofSeconds(configuredTimeout))
            .GET();
        addCustomHeaders(requestBuilder, service.headers());
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = dynamicClient.send(request, HttpResponse.BodyHandlers.ofString());
        long responseTime = System.currentTimeMillis() - startTime;
        boolean isSuccessful = response.statusCode() >= 200 && response.statusCode() < 300;
        return new CheckResult(
            null,
            service.id(),
            checkTime,
            isSuccessful,
            response.statusCode(),
            responseTime,
            null,
            null,
            false,
            null,
            "ping-task"
        );
    } catch (Exception e) {
        long responseTime = System.currentTimeMillis() - startTime;
        String err = e.getMessage();
        if (err == null || err.isBlank()) {
        err = e.getClass().getSimpleName();
        }
        logger.warn("Ping check failed for service {} ({} ms): {}", service.name(), responseTime, err);
        return new CheckResult(
            null,
            service.id(),
            checkTime,
            false,
            0,
            responseTime,
            null,
            err,
            false,
            null,
            "ping-task"
        );
    }
    }
    
    @Override
    public String getTaskType() {
        return TaskType.PING.getCode();
    }
    
    @Override
    public boolean canExecute(MonitoredService service) {
        String url = service.url();
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
    
    private void addCustomHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::header);
        }
        
        // Add default headers
        builder.header("User-Agent", "PingTower-Ping/1.0");
    }
}