package taxisty.pingtower.backend.monitoring.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taxisty.pingtower.backend.monitoring.repository.MonitoredServiceRepository;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing monitored services CRUD operations.
 * Handles service configuration, activation/deactivation, and validation.
 */
@Service
@Transactional
public class MonitoredServiceManager {
    
    private final MonitoredServiceRepository monitoredServiceRepository;
    
    public MonitoredServiceManager(MonitoredServiceRepository monitoredServiceRepository) {
        this.monitoredServiceRepository = monitoredServiceRepository;
    }
    
    /**
     * Create a new monitored service
     */
    public MonitoredService createMonitoredService(MonitoredService service) {
        // Validate URL uniqueness for user
        Optional<MonitoredService> existing = monitoredServiceRepository
            .findByUrlAndUserId(service.url(), service.userId());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Service with this URL already exists for user");
        }
        
        // Set creation and update timestamps
        MonitoredService serviceToSave = new MonitoredService(
            null, // ID will be generated
            service.name(),
            service.description(),
            service.url(),
            service.httpMethod(),
            service.headers(),
            service.expectedResponseCode(),
            service.expectedContent(),
            service.sslCertificateCheck(),
            true, // Set as active by default
            service.userId(),
            LocalDateTime.now(), // createdAt
            LocalDateTime.now()  // updatedAt
        );
        
        return monitoredServiceRepository.save(serviceToSave);
    }
    
    /**
     * Update an existing monitored service
     */
    public MonitoredService updateMonitoredService(Long serviceId, MonitoredService updatedService) {
        MonitoredService existing = monitoredServiceRepository.findById(serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
        
        // Check URL uniqueness if URL is being changed
        if (!existing.url().equals(updatedService.url())) {
            Optional<MonitoredService> duplicateUrl = monitoredServiceRepository
                .findByUrlAndUserId(updatedService.url(), existing.userId());
            if (duplicateUrl.isPresent()) {
                throw new IllegalArgumentException("Service with this URL already exists for user");
            }
        }
        
        // Create updated service preserving ID and timestamps
        MonitoredService serviceToUpdate = new MonitoredService(
            existing.id(),
            updatedService.name(),
            updatedService.description(),
            updatedService.url(),
            updatedService.httpMethod(),
            updatedService.headers(),
            updatedService.expectedResponseCode(),
            updatedService.expectedContent(),
            updatedService.sslCertificateCheck(),
            updatedService.isActive(),
            existing.userId(), // Preserve original user
            existing.createdAt(), // Preserve creation time
            LocalDateTime.now() // Update modification time
        );
        
        return monitoredServiceRepository.save(serviceToUpdate);
    }
    
    /**
     * Activate a monitored service
     */
    public MonitoredService activateService(Long serviceId) {
        MonitoredService service = getServiceById(serviceId);
        MonitoredService activatedService = new MonitoredService(
            service.id(),
            service.name(),
            service.description(),
            service.url(),
            service.httpMethod(),
            service.headers(),
            service.expectedResponseCode(),
            service.expectedContent(),
            service.sslCertificateCheck(),
            true, // Activate
            service.userId(),
            service.createdAt(),
            LocalDateTime.now()
        );
        return monitoredServiceRepository.save(activatedService);
    }
    
    /**
     * Deactivate a monitored service
     */
    public MonitoredService deactivateService(Long serviceId) {
        MonitoredService service = getServiceById(serviceId);
        MonitoredService deactivatedService = new MonitoredService(
            service.id(),
            service.name(),
            service.description(),
            service.url(),
            service.httpMethod(),
            service.headers(),
            service.expectedResponseCode(),
            service.expectedContent(),
            service.sslCertificateCheck(),
            false, // Deactivate
            service.userId(),
            service.createdAt(),
            LocalDateTime.now()
        );
        return monitoredServiceRepository.save(deactivatedService);
    }
    
    /**
     * Get monitored service by ID
     */
    public MonitoredService getServiceById(Long serviceId) {
        return monitoredServiceRepository.findById(serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
    }
    
    /**
     * Get all monitored services for a user
     */
    public List<MonitoredService> getServicesByUserId(Long userId) {
        return monitoredServiceRepository.findActiveByUserId(userId);
    }
    
    /**
     * Get all active monitored services
     */
    public List<MonitoredService> getAllActiveServices() {
        return monitoredServiceRepository.findAllActive();
    }
    
    /**
     * Search monitored services by name
     */
    public List<MonitoredService> searchServicesByName(String name) {
        return monitoredServiceRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Delete a monitored service (soft delete - deactivate)
     */
    public void deleteService(Long serviceId) {
        deactivateService(serviceId);
    }
    
    /**
     * Get services count for a user
     */
    public Long getServicesCountForUser(Long userId) {
        return monitoredServiceRepository.countActiveByUserId(userId);
    }
    
    /**
     * Validate service configuration
     */
    public void validateServiceConfiguration(MonitoredService service) {
        if (service.name() == null || service.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        
        if (service.url() == null || service.url().trim().isEmpty()) {
            throw new IllegalArgumentException("Service URL is required");
        }
        
        if (!service.url().startsWith("http://") && !service.url().startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        
        if (service.userId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
}