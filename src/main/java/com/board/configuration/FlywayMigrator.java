package com.board.configuration;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlywayMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayMigrator.class);

    private FlywayMigrator() {
    }

    public static void migrate(ApplicationProperties properties) {
        Flyway flyway = Flyway.configure()
                .dataSource(properties.getUrl(), properties.getUsername(), properties.getPassword())
                .locations(properties.getFlywayLocations())
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        LOGGER.info("Migrations Flyway aplicadas com sucesso.");
    }
}
