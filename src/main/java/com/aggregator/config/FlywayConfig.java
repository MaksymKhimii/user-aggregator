package com.aggregator.config;

import com.aggregator.util.StringUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.aggregator.constants.ApplicationConstants.*;

/**
 * Configuration class for Flyway database migrations.
 * This class is responsible for running Flyway migrations for all configured data sources.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final DataSourcePropertiesConfig dataSourceProperties;

    /**
     * Executes Flyway migrations for all configured data sources.
     * This method is automatically called after the bean is initialized.
     */
    @PostConstruct
    public void migrateDatabases() {
        List<DataSourcePropertiesConfig.DataSourceConfig> dataSources = dataSourceProperties.getDataSources();

        for (DataSourcePropertiesConfig.DataSourceConfig config : dataSources) {
            log.info("Running Flyway migration for: {}", config.getName());

            if (STRATEGY_MY_SQL.equalsIgnoreCase(config.getStrategy())) {
                Flyway.configure()
                        .dataSource(config.getUrl(), config.getUser(), config.getPassword())
                        .locations(MIGRATION_BASE_PATH + config.getStrategy())
                        .baselineOnMigrate(true)
                        .defaultSchema(StringUtil.extractDatabaseName(config.getUrl()))
                        .load()
                        .migrate();
            }

            if (STRATEGY_POSTGRESQL.equalsIgnoreCase(config.getStrategy())) {
                Flyway.configure()
                        .dataSource(config.getUrl(), config.getUser(), config.getPassword())
                        .locations(MIGRATION_BASE_PATH + config.getStrategy())
                        .baselineOnMigrate(true)
                        .schemas(POSTGRESQL_BASE_SCHEMA)
                        .load()
                        .migrate();
            }
        }
    }
}