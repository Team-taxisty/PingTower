package taxisty.pingtower.backend.monitoring.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.ServiceMetrics;

/**
 * ClickHouse implementation for time series data operations.
 * Optimized for high-volume monitoring data and fast analytics queries.
 */
@Repository
public class ClickHouseRepositoryImpl implements ClickHouseRepository {
    
    private final JdbcTemplate clickHouseJdbcTemplate;
    
    public ClickHouseRepositoryImpl(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }
    
    @Override
    public void saveCheckResults(List<CheckResult> checkResults) {
        if (checkResults.isEmpty()) return;
        
        String sql = """
            INSERT INTO check_results_ts 
            (id, service_id, check_time, is_successful, response_code, response_time_ms, 
             response_body, error_message, ssl_valid, ssl_expiry_date, check_location)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        for (CheckResult result : checkResults) {
            clickHouseJdbcTemplate.update(sql,
                result.id(),
                result.serviceId(),
                Timestamp.valueOf(result.checkTime()),
                result.isSuccessful() ? 1 : 0,
                result.responseCode(),
                result.responseTimeMs(),
                result.responseBody(),
                result.errorMessage(),
                result.sslValid() ? 1 : 0,
                result.sslExpiryDate() != null ? Timestamp.valueOf(result.sslExpiryDate()) : null,
                result.checkLocation()
            );
        }
    }
    
    @Override
    public void saveServiceMetrics(List<ServiceMetrics> serviceMetrics) {
        if (serviceMetrics.isEmpty()) return;
        
        String sql = """
            INSERT INTO service_metrics_ts 
            (id, service_id, period_start, period_end, uptime_percentage, 
             average_response_time_ms, max_response_time_ms, min_response_time_ms, 
             total_checks, successful_checks, failed_checks, aggregation_period)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        for (ServiceMetrics metrics : serviceMetrics) {
            clickHouseJdbcTemplate.update(sql,
                metrics.id(),
                metrics.serviceId(),
                Timestamp.valueOf(metrics.periodStart()),
                Timestamp.valueOf(metrics.periodEnd()),
                metrics.uptimePercentage(),
                metrics.averageResponseTimeMs(),
                metrics.maxResponseTimeMs(),
                metrics.minResponseTimeMs(),
                metrics.totalChecks(),
                metrics.successfulChecks(),
                metrics.failedChecks(),
                metrics.aggregationPeriod()
            );
        }
    }
    
    @Override
    public List<CheckResult> getCheckResultsByServiceId(Long serviceId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT id, service_id, check_time, is_successful, response_code, response_time_ms,
                   response_body, error_message, ssl_valid, ssl_expiry_date, check_location
            FROM check_results_ts
            WHERE service_id = ? AND check_time BETWEEN ? AND ?
            ORDER BY check_time DESC
            """;
        
        return clickHouseJdbcTemplate.query(sql, new CheckResultRowMapper(), 
            serviceId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    @Override
    public List<ServiceMetrics> getServiceMetrics(Long serviceId, LocalDateTime start, LocalDateTime end, String aggregationPeriod) {
        String sql = """
            SELECT id, service_id, period_start, period_end, uptime_percentage,
                   average_response_time_ms, max_response_time_ms, min_response_time_ms,
                   total_checks, successful_checks, failed_checks, aggregation_period
            FROM service_metrics_ts
            WHERE service_id = ? AND period_start >= ? AND period_end <= ? 
            AND aggregation_period = ?
            ORDER BY period_start DESC
            """;
        
        return clickHouseJdbcTemplate.query(sql, new ServiceMetricsRowMapper(),
            serviceId, Timestamp.valueOf(start), Timestamp.valueOf(end), aggregationPeriod);
    }
    
    @Override
    public Double getServiceUptimePercentage(Long serviceId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                (countIf(is_successful = 1) * 100.0) / count(*) as uptime_percentage
            FROM check_results_ts
            WHERE service_id = ? AND check_time BETWEEN ? AND ?
            """;
        
        return clickHouseJdbcTemplate.queryForObject(sql, Double.class,
            serviceId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    @Override
    public Double getAverageResponseTime(Long serviceId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT avg(response_time_ms) as avg_response_time
            FROM check_results_ts
            WHERE service_id = ? AND check_time BETWEEN ? AND ? AND is_successful = 1
            """;
        
        return clickHouseJdbcTemplate.queryForObject(sql, Double.class,
            serviceId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    @Override
    public List<ServiceMetrics> getHourlyMetrics(Long serviceId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                0 as id,
                service_id,
                toStartOfHour(check_time) as period_start,
                toStartOfHour(check_time) + INTERVAL 1 HOUR as period_end,
                (countIf(is_successful = 1) * 100.0) / count(*) as uptime_percentage,
                avg(response_time_ms) as average_response_time_ms,
                max(response_time_ms) as max_response_time_ms,
                min(response_time_ms) as min_response_time_ms,
                count(*) as total_checks,
                countIf(is_successful = 1) as successful_checks,
                countIf(is_successful = 0) as failed_checks,
                '1h' as aggregation_period
            FROM check_results_ts
            WHERE service_id = ? AND check_time BETWEEN ? AND ?
            GROUP BY service_id, toStartOfHour(check_time)
            ORDER BY period_start DESC
            """;
        
        return clickHouseJdbcTemplate.query(sql, new ServiceMetricsRowMapper(),
            serviceId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    @Override
    public void initializeTables() {
        // Create check_results_ts table
        String createCheckResultsTable = """
            CREATE TABLE IF NOT EXISTS check_results_ts (
                id UInt64,
                service_id UInt64,
                check_time DateTime,
                is_successful UInt8,
                response_code UInt16,
                response_time_ms UInt32,
                response_body String,
                error_message String,
                ssl_valid UInt8,
                ssl_expiry_date Nullable(DateTime),
                check_location String
            ) ENGINE = MergeTree()
            PARTITION BY toYYYYMM(check_time)
            ORDER BY (service_id, check_time)
            TTL check_time + INTERVAL 1 YEAR DELETE
            """;
        
        // Create service_metrics_ts table
        String createMetricsTable = """
            CREATE TABLE IF NOT EXISTS service_metrics_ts (
                id UInt64,
                service_id UInt64,
                period_start DateTime,
                period_end DateTime,
                uptime_percentage Float64,
                average_response_time_ms Float64,
                max_response_time_ms UInt32,
                min_response_time_ms UInt32,
                total_checks UInt32,
                successful_checks UInt32,
                failed_checks UInt32,
                aggregation_period String
            ) ENGINE = MergeTree()
            PARTITION BY toYYYYMM(period_start)
            ORDER BY (service_id, aggregation_period, period_start)
            TTL period_start + INTERVAL 2 YEAR DELETE
            """;
        
        clickHouseJdbcTemplate.execute(createCheckResultsTable);
        clickHouseJdbcTemplate.execute(createMetricsTable);
    }
    
    @Override
    public void cleanupOldData(LocalDateTime beforeDate) {
        String sql = "ALTER TABLE check_results_ts DELETE WHERE check_time < ?";
        clickHouseJdbcTemplate.update(sql, Timestamp.valueOf(beforeDate));
        
        String sql2 = "ALTER TABLE service_metrics_ts DELETE WHERE period_start < ?";
        clickHouseJdbcTemplate.update(sql2, Timestamp.valueOf(beforeDate));
    }
    
    private static class CheckResultRowMapper implements RowMapper<CheckResult> {
        @Override
        public CheckResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CheckResult(
                rs.getLong("id"),
                rs.getLong("service_id"),
                rs.getTimestamp("check_time").toLocalDateTime(),
                rs.getInt("is_successful") == 1,
                rs.getInt("response_code"),
                rs.getLong("response_time_ms"),
                rs.getString("response_body"),
                rs.getString("error_message"),
                rs.getInt("ssl_valid") == 1,
                rs.getTimestamp("ssl_expiry_date") != null ? 
                    rs.getTimestamp("ssl_expiry_date").toLocalDateTime() : null,
                rs.getString("check_location")
            );
        }
    }
    
    private static class ServiceMetricsRowMapper implements RowMapper<ServiceMetrics> {
        @Override
        public ServiceMetrics mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ServiceMetrics(
                rs.getLong("id"),
                rs.getLong("service_id"),
                rs.getTimestamp("period_start").toLocalDateTime(),
                rs.getTimestamp("period_end").toLocalDateTime(),
                rs.getDouble("uptime_percentage"),
                rs.getLong("average_response_time_ms"),
                rs.getLong("max_response_time_ms"),
                rs.getLong("min_response_time_ms"),
                rs.getInt("total_checks"),
                rs.getInt("successful_checks"),
                rs.getInt("failed_checks"),
                rs.getString("aggregation_period")
            );
        }
    }
}