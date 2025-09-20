package taxisty.pingtower.backend.api.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import jakarta.validation.Valid;
import taxisty.pingtower.backend.api.dto.MonitoredServiceRequest;
import taxisty.pingtower.backend.api.dto.MonitoredServiceResponse;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.monitoring.service.MonitoringService;
import taxisty.pingtower.backend.scheduler.service.SchedulerService;
import taxisty.pingtower.backend.storage.model.MonitoredService;

/**
 * REST API controller for managing monitored services.
 * This controller provides endpoints for:
 * - Creating new monitored services
 * - Retrieving service information and status
 * - Updating service configurations
 * - Deleting services
 * - Testing service connectivity
 */
@RestController
@RequestMapping("/api/v1/services")
public class MonitoredServiceController {

    private final MonitoredServiceRepository serviceRepository;
    private final SchedulerService schedulerService;
    private final MonitoringService monitoringService;

    public MonitoredServiceController(
            MonitoredServiceRepository serviceRepository,
            SchedulerService schedulerService,
            MonitoringService monitoringService) {
        this.serviceRepository = serviceRepository;
        this.schedulerService = schedulerService;
        this.monitoringService = monitoringService;
    }

    /**
     * Get all monitored services with pagination
     */
    @GetMapping
    public ResponseEntity<Page<MonitoredServiceResponse>> getAllServices(
            Pageable pageable,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search) {
        
        Page<MonitoredService> services;
        
        if (enabled != null && search != null) {
            services = serviceRepository.findByEnabledAndNameContainingIgnoreCase(enabled, search, pageable);
        } else if (enabled != null) {
            services = serviceRepository.findByEnabled(enabled, pageable);
        } else if (search != null) {
            services = serviceRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            services = serviceRepository.findAll(pageable);
        }

        Page<MonitoredServiceResponse> response = services.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific monitored service by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MonitoredServiceResponse> getServiceById(@PathVariable Long id) {
        Optional<MonitoredService> service = serviceRepository.findById(id);
        
        if (service.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToResponse(service.get()));
    }

    /**
     * Create a new monitored service
     */
    @PostMapping
    public ResponseEntity<MonitoredServiceResponse> createService(
            @Valid @RequestBody MonitoredServiceRequest request) {
        
        MonitoredService service = convertToEntity(request);
        service = serviceRepository.save(service);

        // Schedule monitoring for the new service if enabled
        if (service.getEnabled()) {
            try {
                schedulerService.scheduleService(service);
            } catch (Exception e) {
                // Log error but don't fail the creation
                // The service can be scheduled later manually
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(service));
    }

    /**
     * Update an existing monitored service
     */
    @PutMapping("/{id}")
    public ResponseEntity<MonitoredServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody MonitoredServiceRequest request) {
        
        Optional<MonitoredService> existingService = serviceRepository.findById(id);
        
        if (existingService.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MonitoredService service = existingService.get();
        updateEntityFromRequest(service, request);
        service = serviceRepository.save(service);

        // Reschedule if the service is enabled
        if (service.getEnabled()) {
            try {
                schedulerService.rescheduleService(service);
            } catch (Exception e) {
                // Log error but don't fail the update
            }
        } else {
            try {
                schedulerService.unscheduleService(service.getId());
            } catch (Exception e) {
                // Log error but don't fail the update
            }
        }

        return ResponseEntity.ok(convertToResponse(service));
    }

    /**
     * Delete a monitored service
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        Optional<MonitoredService> service = serviceRepository.findById(id);
        
        if (service.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Unschedule the service first
        try {
            schedulerService.unscheduleService(id);
        } catch (Exception e) {
            // Log error but continue with deletion
        }

        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable/disable a monitored service
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<MonitoredServiceResponse> toggleServiceStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        
        Optional<MonitoredService> serviceOpt = serviceRepository.findById(id);
        
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MonitoredService service = serviceOpt.get();
        service.setEnabled(enabled);
        service = serviceRepository.save(service);

        // Schedule or unschedule based on the new status
        try {
            if (enabled) {
                schedulerService.scheduleService(service);
            } else {
                schedulerService.unscheduleService(id);
            }
        } catch (Exception e) {
            // Log error but don't fail the status change
        }

        return ResponseEntity.ok(convertToResponse(service));
    }

    /**
     * Test a service configuration (manual check)
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<String> testService(@PathVariable Long id) {
        Optional<MonitoredService> serviceOpt = serviceRepository.findById(id);
        
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            MonitoredService service = serviceOpt.get();
            
            // Trigger immediate check through the scheduler
            if (schedulerService.isMonitoringScheduled(id)) {
                schedulerService.triggerImmediateCheck(id);
                return ResponseEntity.ok("Manual test check triggered successfully for service: " + service.getName());
            } else {
                // If not scheduled, perform a direct manual check
                return performDirectManualCheck(service);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Performs a direct manual check for services that are not scheduled
     */
    private ResponseEntity<String> performDirectManualCheck(MonitoredService service) {
        try {
            // Create a test HTTP request to the service URL
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(service.getTimeoutSeconds()))
                    .build();
            
            java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(service.getUrl()))
                    .timeout(java.time.Duration.ofSeconds(service.getTimeoutSeconds()))
                    .GET();
            
            // Add custom headers if any
            if (service.getHeaders() != null && !service.getHeaders().isEmpty()) {
                service.getHeaders().forEach(requestBuilder::header);
            }
            
            java.net.http.HttpRequest request = requestBuilder.build();
            
            long startTime = System.currentTimeMillis();
            java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Create and save the check result
            taxisty.pingtower.backend.storage.model.CheckResult checkResult = 
                    new taxisty.pingtower.backend.storage.model.CheckResult(
                            null, // id will be generated
                            service.getId(), // serviceId
                            java.time.LocalDateTime.now(), // checkTime
                            response.statusCode() == service.getExpectedStatusCode(), // isSuccessful
                            response.statusCode(), // responseCode
                            responseTime, // responseTimeMs
                            null, // responseBody (not storing for test)
                            response.statusCode() != service.getExpectedStatusCode() ? 
                                    "Expected status code " + service.getExpectedStatusCode() + 
                                    " but got " + response.statusCode() : null, // errorMessage
                            true, // sslValid (assume valid for test)
                            null, // sslExpiryDate
                            "MANUAL_TEST" // checkLocation
                    );
            
            // Save the check result
            monitoringService.processCheckResult(checkResult);
            
            String statusMessage = checkResult.isSuccessful() ? "SUCCESS" : "FAILED";
            return ResponseEntity.ok(String.format(
                    "Manual test completed for service '%s': %s (HTTP %d, %dms)", 
                    service.getName(), statusMessage, response.statusCode(), responseTime));
            
        } catch (java.net.ConnectException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Connection failed: " + e.getMessage());
        } catch (java.net.http.HttpTimeoutException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("Request timeout: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Test failed: " + e.getMessage());
        }
    }

    private MonitoredServiceResponse convertToResponse(MonitoredService service) {
        return new MonitoredServiceResponse(
                service.getId(),
                service.getName(),
                service.getUrl(),
                service.getType(),
                service.getEnabled(),
                service.getCheckIntervalMinutes(),
                service.getTimeoutSeconds(),
                service.getExpectedStatusCode(),
                service.getHeaders(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }

    private MonitoredService convertToEntity(MonitoredServiceRequest request) {
        MonitoredService service = new MonitoredService();
        updateEntityFromRequest(service, request);
        return service;
    }

    private void updateEntityFromRequest(MonitoredService service, MonitoredServiceRequest request) {
        service.setName(request.name());
        service.setUrl(request.url());
        service.setType(request.type());
        service.setEnabled(request.enabled());
        service.setCheckIntervalMinutes(request.checkIntervalMinutes());
        service.setTimeoutSeconds(request.timeoutSeconds());
        service.setExpectedStatusCode(request.expectedStatusCode());
        service.setHeaders(request.headers());
    }
}