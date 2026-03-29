package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationStartedEvent.class)
    public void migrateRoleColumn() {
        try {
            // Disable FK checks so we can clean bad rows safely
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Remove rows with invalid/empty role values (ENUM ordinal 0 from failed inserts)
            jdbcTemplate.execute(
                "DELETE FROM users WHERE role NOT IN ('PATIENT', 'DOCTOR') OR role IS NULL OR role = ''"
            );

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            // Now convert the ENUM column to VARCHAR so Hibernate can manage it correctly
            jdbcTemplate.execute(
                "ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL"
            );

            System.out.println("[DB MIGRATION] Role column successfully migrated to VARCHAR(20).");
        } catch (Exception e) {
            // Column is likely already in the correct state - safe to ignore
            System.out.println("[DB MIGRATION] Role column migration skipped: " + e.getMessage());
        }
    }
}
