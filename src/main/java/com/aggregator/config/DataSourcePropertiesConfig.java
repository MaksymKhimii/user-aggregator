package com.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for data source properties.
 * This class is used to load data source configurations from the application properties
 * with the prefix "aggregator".
 */
@Data
@Component
@ConfigurationProperties(prefix = "aggregator")
public class DataSourcePropertiesConfig {
    private List<DataSourceConfig> dataSources = new ArrayList<>();

    /**
     * Inner class representing the configuration for a single data source.
     */
    @Data
    public static class DataSourceConfig {
        private String name;

        /**
         * The database strategy ("postgres", "mysql", ...).
         */
        private String strategy;
        private String url;
        private String table;
        private String user;
        private String password;

        /**
         * A map of column mappings for the database table.
         * The keys represent the logical column names,
         * and the values represent the actual column names in the database.
         */
        private Map<String, String> mapping;
    }
}