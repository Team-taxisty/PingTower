package taxisty.pingtower.backend.monitoring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import taxisty.pingtower.backend.monitoring.repository.ClickHouseRepository;

/**
 * Component to initialize ClickHouse tables on application startup
 */
@Component
public class ClickHouseInitializer {

    private final ClickHouseRepository clickHouseRepository;

    @Autowired
    public ClickHouseInitializer(ClickHouseRepository clickHouseRepository) {
        this.clickHouseRepository = clickHouseRepository;
    }

    @PostConstruct
    public void initialize() {
        try {
            clickHouseRepository.initializeTables();
            System.out.println("ClickHouse tables initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize ClickHouse tables: " + e.getMessage());
            // Don't fail the application startup if ClickHouse is not available
        }
    }
}