package taxisty.pingtower.backend.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the monitoring system.
 */
@Configuration
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {
    
    private ClickHouse clickhouse = new ClickHouse();
    private Analytics analytics = new Analytics();
    
    public static class ClickHouse {
        private String url = "jdbc:clickhouse://localhost:8123/monitoring";
        private String username = "default";
        private String password = "";
        private String driverClassName = "com.clickhouse.jdbc.ClickHouseDriver";
        private int socketTimeout = 30000;
        private int connectionTimeout = 10000;
        
        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
        
        public int getSocketTimeout() { return socketTimeout; }
        public void setSocketTimeout(int socketTimeout) { this.socketTimeout = socketTimeout; }
        
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    }
    
    public static class Analytics {
        private int batchSize = 1000;
        private int flushIntervalSeconds = 60;
        private boolean enableRealTimeMetrics = true;
        private String aggregationPeriods = "1h,6h,1d,7d,30d";
        
        // Getters and setters
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        
        public int getFlushIntervalSeconds() { return flushIntervalSeconds; }
        public void setFlushIntervalSeconds(int flushIntervalSeconds) { this.flushIntervalSeconds = flushIntervalSeconds; }
        
        public boolean isEnableRealTimeMetrics() { return enableRealTimeMetrics; }
        public void setEnableRealTimeMetrics(boolean enableRealTimeMetrics) { this.enableRealTimeMetrics = enableRealTimeMetrics; }
        
        public String getAggregationPeriods() { return aggregationPeriods; }
        public void setAggregationPeriods(String aggregationPeriods) { this.aggregationPeriods = aggregationPeriods; }
    }
    
    public ClickHouse getClickhouse() { return clickhouse; }
    public void setClickhouse(ClickHouse clickhouse) { this.clickhouse = clickhouse; }
    
    public Analytics getAnalytics() { return analytics; }
    public void setAnalytics(Analytics analytics) { this.analytics = analytics; }
}