package com.aggregator.repository;

import com.aggregator.config.DataSourcePropertiesConfig;
import com.aggregator.model.UserDto;
import com.aggregator.model.UserRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.aggregator.constants.ApplicationConstants.*;

/**
 * Repository class for managing user data across multiple databases.
 * This class provides methods to fetch and add users to all configured data sources.
 */
@Slf4j
@Repository
public class UserRepository {
    private final Map<String, DataSource> dataSources;
    private final Map<String, DataSourcePropertiesConfig.DataSourceConfig> dataSourceConfigMap;

    /**
     * Constructs a new {@link UserRepository} with the provided data sources and configurations.
     *
     * @param dataSources A map of data sources.
     * @param properties  The data source configurations.
     */
    @Autowired
    public UserRepository(Map<String, DataSource> dataSources, DataSourcePropertiesConfig properties) {
        this.dataSources = dataSources;
        this.dataSourceConfigMap = properties.getDataSources().stream()
                .collect(Collectors.toMap(DataSourcePropertiesConfig.DataSourceConfig::getName, config -> config));
        log.info("UserRepository initialized with {} data sources", dataSources.size());
    }

    /**
     * Fetches all users from all configured data sources.
     *
     * @return A list of {@link UserDto} objects representing the users.
     */
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users from databases");
        return dataSources.entrySet().stream()
                .flatMap(entry -> fetchUsersFromDataSource(entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toList());
    }

    /**
     * Fetches users from a specific data source.
     *
     * @param dbName      The name of the database.
     * @param dataSource  The data source to fetch users from.
     * @return A list of {@link UserDto} objects representing the users.
     */
    private List<UserDto> fetchUsersFromDataSource(String dbName, DataSource dataSource) {
        DataSourcePropertiesConfig.DataSourceConfig config = getConfigForDatabase(dbName);
        String query = buildSelectQuery(config);
        return new JdbcTemplate(dataSource).query(query, (rs, rowNum) -> new UserDto(
                rs.getString(ID),
                rs.getString(USERNAME),
                rs.getString(NAME),
                rs.getString(SURNAME)
        ));
    }

    /**
     * Adds a user to all configured data sources.
     *
     * @param userRequest The user data to add.
     */
    @Transactional
    public void addUserToAllDatabases(UserRequest userRequest) {
        log.info("Adding user to all databases: {}", userRequest);
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            addUserToDataSource(entry.getKey(), entry.getValue(), userRequest);
        }
    }

    /**
     * Adds a user to a specific data source.
     *
     * @param dbName      The name of the database.
     * @param dataSource  The data source to add the user to.
     * @param userRequest The user data to add.
     */
    private void addUserToDataSource(String dbName, DataSource dataSource, UserRequest userRequest) {
        DataSourcePropertiesConfig.DataSourceConfig config = getConfigForDatabase(dbName);
        String query = buildInsertQuery(config);
        new JdbcTemplate(dataSource).update(query,
                userRequest.getId(),
                userRequest.getUsername(),
                userRequest.getName(),
                userRequest.getSurname()
        );
        log.info("User with id: {} was successfully added to database: {}", userRequest.getId(), dbName);
    }

    /**
     * Retrieves the configuration for a specific database.
     *
     * @param dbName The name of the database.
     * @return The configuration for the database.
     * @throws RuntimeException If the configuration is not found.
     */
    private DataSourcePropertiesConfig.DataSourceConfig getConfigForDatabase(String dbName) {
        return Optional.ofNullable(dataSourceConfigMap.get(dbName))
                .orElseThrow(() -> new RuntimeException("Config not found for " + dbName));
    }

    /**
     * Builds a SQL SELECT query for fetching users from a database.
     *
     * @param config The database configuration.
     * @return The SQL SELECT query.
     */
    private String buildSelectQuery(DataSourcePropertiesConfig.DataSourceConfig config) {
        return String.format(SELECT_ALL_USERS_QUERY,
                config.getMapping().get(ID),
                config.getMapping().get(USERNAME),
                config.getMapping().get(NAME),
                config.getMapping().get(SURNAME),
                config.getTable()
        );
    }

    /**
     * Builds a SQL INSERT query for adding a user to a database.
     *
     * @param config The database configuration.
     * @return The SQL INSERT query.
     */
    private String buildInsertQuery(DataSourcePropertiesConfig.DataSourceConfig config) {
        return String.format(INSERT_USER_QUERY,
                config.getTable(),
                config.getMapping().get(ID),
                config.getMapping().get(USERNAME),
                config.getMapping().get(NAME),
                config.getMapping().get(SURNAME)
        );
    }
}
