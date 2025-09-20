package taxisty.pingtower.backend.monitoring.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.monitoring.config.MonitoringProperties;
import taxisty.pingtower.backend.monitoring.repository.ClickHouseRepository;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for aggregating monitoring data and generating analytics reports.
 * Handles metrics calculation, SLA reporting, and historical data analysis.
 */
@Service
public class MonitoringAnalyticsService {
    
    private final ClickHouseRepository clickHouseRepository;
    private final MonitoringProperties monitoringProperties;
    
    public MonitoringAnalyticsService(
            ClickHouseRepository clickHouseRepository,
            MonitoringProperties monitoringProperties) {
        this.clickHouseRepository = clickHouseRepository;
        this.monitoringProperties = monitoringProperties;
    }
    
    /**
     * Calculate and store aggregated metrics for a service
     */
    public CompletableFuture<Void> calculateServiceMetrics(Long serviceId, LocalDateTime start, LocalDateTime end, String aggregationPeriod) {
        return CompletableFuture.runAsync(() -> {
            try {
                ServiceMetrics metrics = computeServiceMetrics(serviceId, start, end, aggregationPeriod);
                if (metrics != null) {
                    clickHouseRepository.saveServiceMetrics(List.of(metrics));
                }
            } catch (Exception e) {
                // Log error but don't fail the entire process
                System.err.println("Error calculating metrics for service " + serviceId + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Generate SLA report for a service over a time period
     */
    public SLAReport generateSLAReport(Long serviceId, LocalDateTime start, LocalDateTime end) {
        Double uptimePercentage = clickHouseRepository.getServiceUptimePercentage(serviceId, start, end);
        Double averageResponseTime = clickHouseRepository.getAverageResponseTime(serviceId, start, end);
        List<ServiceMetrics> dailyMetrics = clickHouseRepository.getServiceMetrics(serviceId, start, end, "1d");
        
        // Calculate SLA compliance (assuming 99.9% uptime target)
        double slaTarget = 99.9;
        boolean slaCompliant = uptimePercentage != null && uptimePercentage >= slaTarget;
        
        // Calculate downtime in minutes
        long totalMinutes = ChronoUnit.MINUTES.between(start, end);
        double downtimeMinutes = totalMinutes * ((100.0 - (uptimePercentage != null ? uptimePercentage : 0.0)) / 100.0);
        
        return new SLAReport(
            serviceId,
            start,
            end,
            uptimePercentage != null ? uptimePercentage : 0.0,
            averageResponseTime != null ? averageResponseTime : 0.0,
            slaTarget,
            slaCompliant,
            downtimeMinutes,
            dailyMetrics
        );
    }
    
    /**
     * Get performance trends for a service
     */
    public PerformanceTrend getPerformanceTrend(Long serviceId, LocalDateTime start, LocalDateTime end) {
        List<ServiceMetrics> hourlyMetrics = clickHouseRepository.getHourlyMetrics(serviceId, start, end);
        
        if (hourlyMetrics.isEmpty()) {
            return new PerformanceTrend(serviceId, TrendDirection.STABLE, 0.0, 0.0);
        }
        
        // Calculate trend for uptime
        double avgUptimeFirst = hourlyMetrics.stream()
            .limit(hourlyMetrics.size() / 2)
            .mapToDouble(ServiceMetrics::uptimePercentage)
            .average().orElse(0.0);
        
        double avgUptimeSecond = hourlyMetrics.stream()
            .skip(hourlyMetrics.size() / 2)
            .mapToDouble(ServiceMetrics::uptimePercentage)
            .average().orElse(0.0);
        
        // Calculate trend for response time
        double avgResponseFirst = hourlyMetrics.stream()
            .limit(hourlyMetrics.size() / 2)
            .mapToDouble(ServiceMetrics::averageResponseTimeMs)
            .average().orElse(0.0);
        
        double avgResponseSecond = hourlyMetrics.stream()
            .skip(hourlyMetrics.size() / 2)
            .mapToDouble(ServiceMetrics::averageResponseTimeMs)
            .average().orElse(0.0);
        
        TrendDirection uptimeTrend = determineTrend(avgUptimeFirst, avgUptimeSecond);
        
        return new PerformanceTrend(
            serviceId,
            uptimeTrend,
            avgUptimeSecond - avgUptimeFirst,
            avgResponseSecond - avgResponseFirst
        );
    }
    
    /**
     * Scheduled task to calculate metrics for all services
     */
    @Scheduled(fixedRate = 60000) // Run every minute, TODO: use monitoringProperties.analytics.flushIntervalSeconds
    public void calculateScheduledMetrics() {
        LocalDateTime now = LocalDateTime.now();
        
        // This would typically get all active service IDs from PostgreSQL
        // For now, we'll process a placeholder
        // TODO: Integrate with service repository to get active service IDs
        
        System.out.println("Running scheduled metrics calculation at " + now);
    }
    
    /**
     * Clean up old analytics data
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldAnalyticsData() {
        int retentionDays = 365; // Keep 1 year of data
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        clickHouseRepository.cleanupOldData(cutoffDate);
    }
    
    private ServiceMetrics computeServiceMetrics(Long serviceId, LocalDateTime start, LocalDateTime end, String aggregationPeriod) {
        Double uptimePercentage = clickHouseRepository.getServiceUptimePercentage(serviceId, start, end);
        Double averageResponseTime = clickHouseRepository.getAverageResponseTime(serviceId, start, end);
        
        // Get raw check results to calculate additional metrics
        var checkResults = clickHouseRepository.getCheckResultsByServiceId(serviceId, start, end);
        
        if (checkResults.isEmpty()) {
            return null;
        }
        
        int totalChecks = checkResults.size();
        int successfulChecks = (int) checkResults.stream().filter(r -> r.isSuccessful()).count();
        int failedChecks = totalChecks - successfulChecks;
        
        long maxResponseTime = checkResults.stream()
            .filter(r -> r.isSuccessful())
            .mapToLong(r -> r.responseTimeMs())
            .max().orElse(0L);
        
        long minResponseTime = checkResults.stream()
            .filter(r -> r.isSuccessful())
            .mapToLong(r -> r.responseTimeMs())
            .min().orElse(0L);
        
        return new ServiceMetrics(
            null, // ID will be generated
            serviceId,
            start,
            end,
            uptimePercentage != null ? uptimePercentage : 0.0,
            averageResponseTime != null ? averageResponseTime.longValue() : 0L,
            maxResponseTime,
            minResponseTime,
            totalChecks,
            successfulChecks,
            failedChecks,
            aggregationPeriod
        );
    }
    
    private TrendDirection determineTrend(double first, double second) {
        double threshold = 1.0; // 1% change threshold
        double percentChange = Math.abs((second - first) / first * 100);
        
        if (percentChange < threshold) {
            return TrendDirection.STABLE;
        }
        
        return second > first ? TrendDirection.IMPROVING : TrendDirection.DEGRADING;
    }
    
    /**
     * SLA Report data class
     */
    public record SLAReport(
        Long serviceId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Double uptimePercentage,
        Double averageResponseTime,
        Double slaTarget,
        Boolean slaCompliant,
        Double downtimeMinutes,
        List<ServiceMetrics> dailyMetrics
    ) {}
    
    /**
     * Performance Trend data class
     */
    public record PerformanceTrend(
        Long serviceId,
        TrendDirection uptimeTrend,
        Double uptimeChange,
        Double responseTimeChange
    ) {}
    
    /**
     * Trend direction enumeration
     */
    public enum TrendDirection {
        IMPROVING,
        STABLE,
        DEGRADING
    }
}