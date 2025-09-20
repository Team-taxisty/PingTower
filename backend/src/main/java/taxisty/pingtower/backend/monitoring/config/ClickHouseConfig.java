package taxisty.pingtower.backend.monitoring.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Configuration for ClickHouse database connection for time series analytics.
 * ClickHouse is used for storing large volumes of monitoring data and performing
 * high-performance analytics queries.
 */
@Configuration
public class ClickHouseConfig {
    
    @Value("${monitoring.clickhouse.url:jdbc:clickhouse://localhost:8123/monitoring}")
    private String clickHouseUrl;
    
    @Value("${monitoring.clickhouse.username:default}")
    private String username;
    
    @Value("${monitoring.clickhouse.password:}")
    private String password;
    
    @Value("${monitoring.clickhouse.driver-class-name:com.clickhouse.jdbc.ClickHouseDriver}")
    private String driverClassName;
    
    // Do NOT expose ClickHouse DataSource as a Spring bean to avoid
    // interfering with Spring Boot's primary JPA DataSource (PostgreSQL).
    private DataSource clickHouseDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(clickHouseUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        
        Properties properties = new Properties();
        properties.setProperty("compress", "true");
        properties.setProperty("socket_timeout", "30000");
        properties.setProperty("connection_timeout", "10000");
        dataSource.setConnectionProperties(properties);
        
        return dataSource;
    }
    
    @Bean(name = "clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate() {
        return new JdbcTemplate(clickHouseDataSource());
    }
}
