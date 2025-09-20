package taxisty.pingtower.backend.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import taxisty.pingtower.backend.api.dto.CheckResultResponse;
import taxisty.pingtower.backend.api.dto.ServiceMetricsResponse;
import taxisty.pingtower.backend.monitoring.repository.CheckResultRepository;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.monitoring.service.MonitoringService;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

/**
 * REST API controller for monitoring data and analytics
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringDataController {

    private final MonitoringService monitoringService;
    private final CheckResultRepository checkResultRepository;
    private final MonitoredServiceRepository serviceRepository;

    public MonitoringDataController(
            MonitoringService monitoringService,
            CheckResultRepository checkResultRepository,
            MonitoredServiceRepository serviceRepository) {
        this.monitoringService = monitoringService;
        this.checkResultRepository = checkResultRepository;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Get recent check results for all services
     */
    @GetMapping("/results")
    public ResponseEntity<Page<CheckResultResponse>> getRecentResults(
            Pageable pageable,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Boolean successful,
            @RequestParam(required = false) String since) {
        
        Page<CheckResult> results;
        LocalDateTime sinceTime = parseDateTimeParam(since, LocalDateTime.now().minusHours(24));
        
        if (serviceId != null && successful != null) {
            if (successful) {
                results = checkResultRepository.findRecentSuccessByServiceId(serviceId, sinceTime, pageable);
            } else {
                results = checkResultRepository.findRecentFailuresByServiceId(serviceId, sinceTime, pageable);
            }
        } else if (serviceId != null) {
            results = checkResultRepository.findByServiceIdAndCheckedAtAfterOrderByCheckedAtDesc(serviceId, sinceTime, pageable);
        } else if (successful != null) {
            if (successful) {
                results = checkResultRepository.findRecentSuccessfulResults(sinceTime, pageable);
            } else {
                results = checkResultRepository.findRecentFailedResults(sinceTime, pageable);
            }
        } else {
            results = checkResultRepository.findByCheckedAtAfterOrderByCheckedAtDesc(sinceTime, pageable);
        }

        Page<CheckResultResponse> response = results.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get check results for a specific service
     */
    @GetMapping("/services/{serviceId}/results")
    public ResponseEntity<Page<CheckResultResponse>> getServiceResults(
            @PathVariable Long serviceId,
            Pageable pageable,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until) {
        
        // Verify service exists
        if (!serviceRepository.existsById(serviceId)) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime sinceTime = parseDateTimeParam(since, LocalDateTime.now().minusDays(7));
        LocalDateTime untilTime = parseDateTimeParam(until, LocalDateTime.now());
        
        Page<CheckResult> results = checkResultRepository
                .findByServiceIdAndCheckedAtBetweenOrderByCheckedAtDesc(serviceId, sinceTime, untilTime, pageable);

        Page<CheckResultResponse> response = results.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get service metrics and analytics
     */
    @GetMapping("/services/{serviceId}/metrics")
    public ResponseEntity<ServiceMetricsResponse> getServiceMetrics(
            @PathVariable Long serviceId,
            @RequestParam(required = false) String since) {
        
        Optional<MonitoredService> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime sinceTime = parseDateTimeParam(since, LocalDateTime.now().minusDays(30));
        
        try {
            ServiceMetrics metrics = monitoringService.getServiceMetrics(serviceId, sinceTime, LocalDateTime.now());
            ServiceMetricsResponse response = convertMetricsToResponse(serviceOpt.get(), metrics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get overall system health metrics
     */
    @GetMapping("/health")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        try {
            // Get counts of active services
            long totalServices = serviceRepository.count();
            long activeServices = serviceRepository.findAllActive().size();
            
            // Get recent failure count
            LocalDateTime since = LocalDateTime.now().minusHours(1);
            long recentFailures = checkResultRepository.countRecentFailures(since);
            long recentChecks = checkResultRepository.countRecentChecks(since);
            
            double successRate = recentChecks > 0 ? 
                    ((double)(recentChecks - recentFailures) / recentChecks) * 100 : 100.0;

            SystemHealthResponse response = new SystemHealthResponse(
                    totalServices,
                    activeServices,
                    recentChecks,
                    recentFailures,
                    successRate,
                    LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get latest status for all services (dashboard summary)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<ServiceStatusResponse>> getDashboardData() {
        try {
            List<MonitoredService> activeServices = serviceRepository.findAllActive();
            List<ServiceStatusResponse> statusList = activeServices.stream()
                    .map(this::getServiceStatus)
                    .toList();
            
            return ResponseEntity.ok(statusList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private CheckResultResponse convertToResponse(CheckResult result) {
        Optional<MonitoredService> serviceOpt = serviceRepository.findById(result.getServiceId());
        String serviceName = serviceOpt.map(MonitoredService::getName).orElse("Unknown Service");
        
        return new CheckResultResponse(
                result.getId(),
                result.getServiceId(),
                serviceName,
                result.getCheckTime(),
                result.isSuccessful(),
                result.getResponseCode(),
                (int) result.getResponseTimeMs(),
                result.getErrorMessage()
        );
    }

    private ServiceMetricsResponse convertMetricsToResponse(MonitoredService service, ServiceMetrics metrics) {
        return new ServiceMetricsResponse(
                service.getId(),
                service.getName(),
                service.getUrl(),
                (long) metrics.getTotalChecks(),
                (long) metrics.getSuccessfulChecks(),
                (long) metrics.getFailedChecks(),
                (double) metrics.getAverageResponseTimeMs(),
                (int) metrics.getMinResponseTimeMs(),
                (int) metrics.getMaxResponseTimeMs(),
                metrics.getUptimePercentage(),
                metrics.getPeriodStart(),
                metrics.getPeriodEnd()
        );
    }

    private ServiceStatusResponse getServiceStatus(MonitoredService service) {
        try {
            // Get the most recent check result
            Optional<CheckResult> latestResult = checkResultRepository
                    .findTopByServiceIdOrderByCheckTimeDesc(service.getId());
            
            if (latestResult.isEmpty()) {
                return new ServiceStatusResponse(
                        service.getId(),
                        service.getName(),
                        service.getUrl(),
                        "UNKNOWN",
                        null,
                        null,
                        null,
                        service.getEnabled()
                );
            }

            CheckResult result = latestResult.get();
            String status = result.isSuccessful() ? "UP" : "DOWN";
            
            return new ServiceStatusResponse(
                    service.getId(),
                    service.getName(),
                    service.getUrl(),
                    status,
                    result.getResponseCode(),
                    (int) result.getResponseTimeMs(),
                    result.getCheckTime(),
                    service.getEnabled()
            );
        } catch (Exception e) {
            return new ServiceStatusResponse(
                    service.getId(),
                    service.getName(),
                    service.getUrl(),
                    "ERROR",
                    null,
                    null,
                    null,
                    service.getEnabled()
            );
        }
    }

    private LocalDateTime parseDateTimeParam(String dateTimeStr, LocalDateTime defaultValue) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return defaultValue;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }

    // Response DTOs
    
    public record SystemHealthResponse(
            long totalServices,
            long activeServices,
            long recentChecks,
            long recentFailures,
            double successRate,
            LocalDateTime timestamp
    ) {}

    public record ServiceStatusResponse(
            Long serviceId,
            String serviceName,
            String serviceUrl,
            String status,
            Integer responseCode,
            Integer responseTimeMs,
            LocalDateTime lastChecked,
            Boolean enabled
    ) {}
}