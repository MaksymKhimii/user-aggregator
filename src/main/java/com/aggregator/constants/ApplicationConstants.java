package com.aggregator.constants;

public class ApplicationConstants {

    public static final String STRATEGY_MY_SQL = "mysql";
    public static final String STRATEGY_POSTGRESQL = "postgres";
    public static final String MIGRATION_BASE_PATH = "classpath:db/migration/";
    public static final String POSTGRESQL_BASE_SCHEMA = "public";
    public static final String URL_SEPARATOR = "/";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";

    public static final String INSERT_USER_QUERY = "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)";
    public static final String SELECT_ALL_USERS_QUERY = "SELECT %s AS id, %s AS username, %s AS name, %s AS surname FROM %s";
}
