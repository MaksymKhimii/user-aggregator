package com.aggregator.controller;

import com.aggregator.config.DataSourcePropertiesConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test class for {@link UserController}.
 * This class tests the functionality of the UserController API using Testcontainers
 * to spin up PostgreSQL and MySQL databases for testing.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("userdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8")
            .withDatabaseName("userdb")
            .withUsername("mysqluser")
            .withPassword("mysqlpass");

    @Autowired
    private DataSourcePropertiesConfig dataSourcePropertiesConfig;

    @Autowired
    private Map<String, DataSource> dataSources;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureTestDatabases(DynamicPropertyRegistry registry) {
        configureDatabase(registry, "data-base-1",
                "postgres",
                POSTGRES,
                "users",
                "user_id",
                "login",
                "first_name",
                "last_name");

        configureDatabase(registry,
                "data-base-2",
                "mysql",
                MYSQL,
                "user_table",
                "ldap_id",
                "ldap_login",
                "name",
                "surname");
    }


    /**
     * Helper method to configure a single database in the {@link DynamicPropertyRegistry}.
     *
     * @param registry       The {@link DynamicPropertyRegistry} to register properties.
     * @param name           The name of the data source.
     * @param strategy       The database strategy (e.g., "postgres", "mysql").
     * @param container      The database container (PostgreSQL or MySQL).
     * @param table          The table name in the database.
     * @param idColumn       The column name for the user ID.
     * @param usernameColumn The column name for the username.
     * @param nameColumn     The column name for the user's first name.
     * @param surnameColumn  The column name for the user's last name.
     */
    private static void configureDatabase(DynamicPropertyRegistry registry, String name, String strategy,
                                          JdbcDatabaseContainer<?> container, String table,
                                          String idColumn, String usernameColumn, String nameColumn, String surnameColumn) {
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].name", () -> name);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].strategy", () -> strategy);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].url", container::getJdbcUrl);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].table", () -> table);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].user", container::getUsername);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].password", container::getPassword);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].mapping.id", () -> idColumn);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].mapping.username", () -> usernameColumn);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].mapping.name", () -> nameColumn);
        registry.add("aggregator.data-sources[" + (strategy.equals("postgres") ? "0" : "1") + "].mapping.surname", () -> surnameColumn);
    }

    @Test
    void contextLoads() {
        Assertions.assertNotNull(dataSourcePropertiesConfig);
        Assertions.assertFalse(dataSourcePropertiesConfig.getDataSources().isEmpty());

        assertDatabaseConfig(dataSourcePropertiesConfig.getDataSources().get(0),
                "data-base-1",
                "postgres",
                POSTGRES,
                "users",
                "user_id",
                "login",
                "first_name",
                "last_name");

        assertDatabaseConfig(dataSourcePropertiesConfig.getDataSources().get(1),
                "data-base-2",
                "mysql",
                MYSQL,
                "user_table",
                "ldap_id",
                "ldap_login",
                "name",
                "surname");
    }

    @Test
    void testDataSourcesAreAvailable() throws SQLException {
        Assertions.assertNotNull(dataSources);
        Assertions.assertEquals(2, dataSources.size());

        checkDatabaseConnection("data-base-1");
        checkDatabaseConnection("data-base-2");
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("446655440000"))
                .andExpect(jsonPath("$[0].username").value("test_postgres_user"))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].surname").value("Smith"))
                .andExpect(jsonPath("$[1].id").value("763991100"))
                .andExpect(jsonPath("$[1].username").value("test_mysql_user"))
                .andExpect(jsonPath("$[1].name").value("Maks"))
                .andExpect(jsonPath("$[1].surname").value("Ivko"));
    }

    private void assertDatabaseConfig(DataSourcePropertiesConfig.DataSourceConfig config,
                                      String expectedName,
                                      String expectedStrategy,
                                      JdbcDatabaseContainer<?> expectedContainer,
                                      String expectedTable,
                                      String expectedId,
                                      String expectedUsername,
                                      String expectedNameField,
                                      String expectedSurname) {
        assertSoftly(softly -> {
            softly.assertThat(config.getName()).isEqualTo(expectedName);
            softly.assertThat(config.getStrategy()).isEqualTo(expectedStrategy);
            softly.assertThat(config.getUrl()).isEqualTo(expectedContainer.getJdbcUrl());
            softly.assertThat(config.getTable()).isEqualTo(expectedTable);
            softly.assertThat(config.getUser()).isEqualTo(expectedContainer.getUsername());
            softly.assertThat(config.getPassword()).isEqualTo(expectedContainer.getPassword());

            Map<String, String> mapping = config.getMapping();
            softly.assertThat(mapping.get("id")).isEqualTo(expectedId);
            softly.assertThat(mapping.get("username")).isEqualTo(expectedUsername);
            softly.assertThat(mapping.get("name")).isEqualTo(expectedNameField);
            softly.assertThat(mapping.get("surname")).isEqualTo(expectedSurname);
        });
    }

    private void checkDatabaseConnection(String dbName) throws SQLException {
        DataSource dataSource = dataSources.get(dbName);
        Assertions.assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            Assertions.assertFalse(connection.isClosed());
        }
    }
}
