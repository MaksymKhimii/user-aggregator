package com.aggregator.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.aggregator.constants.ApplicationConstants.STRATEGY_MY_SQL;
import static com.aggregator.constants.ApplicationConstants.STRATEGY_POSTGRESQL;

/**
 * Configuration class for creating multiple data sources.
 * This class creates a map of data sources based on
 * the configurations provided in {@link DataSourcePropertiesConfig}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiDataSourceConfig {

    private final DataSourcePropertiesConfig properties;

    /**
     * Creates a map of data sources,
     * where the key is the data source name and the value is the {@link DataSource}.
     *
     * @return A map of data sources.
     */
    @Bean
    public Map<String, DataSource> dataSources() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        log.info("Creating DataSources from properties: {}", properties.getDataSources());
        for (DataSourcePropertiesConfig.DataSourceConfig config : properties.getDataSources()) {
            dataSourceMap.put(config.getName(), createDataSource(config));
        }
        log.info("Created DataSources: {}", dataSourceMap.keySet());
        return dataSourceMap;
    }

    /**
     * Creates a {@link DataSource} instance based on the provided configuration.
     *
     * @param config The configuration for the data source.
     * @return A configured {@link DataSource} instance.
     * @throws IllegalArgumentException If the database strategy is unsupported.
     */
    private DataSource createDataSource(DataSourcePropertiesConfig.DataSourceConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUser());
        dataSource.setPassword(config.getPassword());

        switch (config.getStrategy().toLowerCase()) {
            case STRATEGY_POSTGRESQL:
                dataSource.setDriverClassName("org.postgresql.Driver");
                log.debug("Using PostgreSQL driver for database: {}", config.getName());
                break;
            case STRATEGY_MY_SQL:
                dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                log.debug("Using MySQL driver for database: {}", config.getName());
                break;
            default:
                log.error("Unsupported database strategy: {}", config.getStrategy());
                throw new IllegalArgumentException("Unsupported database strategy: " + config.getStrategy());
        }
        return dataSource;
    }

    /**
     * Logs the loaded data source configurations.
     * This method is automatically called after the bean is initialized.
     */
    @PostConstruct
    public void logProperties() {
        log.info("Loaded DataSources: {}", properties.getDataSources());
    }
}