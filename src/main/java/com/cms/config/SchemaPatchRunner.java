package com.cms.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Runs safe ALTER TABLE patches on startup so the H2 database never needs
 * to be wiped to fix schema issues. Each statement is executed independently;
 * failures are logged and ignored so existing data is never at risk.
 */
@Component
public class SchemaPatchRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaPatchRunner.class);

    @Autowired
    private DataSource dataSource;

    private static final List<String> PATCHES = List.of(
        // Make dealer_id and vehicle_id nullable for used car deals
        "ALTER TABLE dealer_deals ALTER COLUMN dealer_id INTEGER NULL",
        "ALTER TABLE dealer_deals ALTER COLUMN vehicle_id INTEGER NULL",
        // Ensure car_type column exists with default NEW
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS car_type VARCHAR(10) DEFAULT 'NEW'",
        // Ensure all used-car columns exist
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_year INTEGER",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_km_driven INTEGER",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_fuel_type VARCHAR(50)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_owner_type VARCHAR(20)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_transmission VARCHAR(20)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_color VARCHAR(50)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_asking_price DECIMAL(12,2)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_registration_state VARCHAR(50)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_source_website VARCHAR(100)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_listing_url VARCHAR(500)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_deal_tag VARCHAR(20)",
        "ALTER TABLE dealer_deals ADD COLUMN IF NOT EXISTS uc_deal_score INTEGER"
    );

    @PostConstruct
    public void patch() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : PATCHES) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    // Column already correct or table doesn't exist yet — safe to ignore
                    log.debug("Schema patch skipped ({}): {}", e.getMessage(), sql);
                }
            }
            log.info("Schema patches applied successfully.");
        } catch (Exception e) {
            log.warn("Schema patch runner could not open connection: {}", e.getMessage());
        }
    }
}
