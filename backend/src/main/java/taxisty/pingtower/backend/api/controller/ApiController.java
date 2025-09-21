package taxisty.pingtower.backend.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API documentation and health check controller
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * API health check endpoint
     */
    @GetMapping("/health")
    public ApiHealthResponse health() {
        return new ApiHealthResponse(
                "PingTower API",
                "1.0.0",
                "UP",
                System.currentTimeMillis()
        );
    }

    /**
     * Get API information and available endpoints
     */
    @GetMapping("/info")
    public ApiInfoResponse info() {
        return new ApiInfoResponse(
                "PingTower Monitoring System API",
                "1.0.0",
                "Comprehensive web service monitoring with 24/7 availability tracking",
                new String[]{
                    "/api/v1/services - Monitored services management",
                    "/api/v1/monitoring - Monitoring data and analytics", 
                    "/api/v1/alerts - Alert management",
                    "/api/v1/notifications - Notification channels"
                }
        );
    }

    public record ApiHealthResponse(
            String service,
            String version,
            String status,
            long timestamp
    ) {}

    public record ApiInfoResponse(
            String name,
            String version,
            String description,
            String[] endpoints
    ) {}
}