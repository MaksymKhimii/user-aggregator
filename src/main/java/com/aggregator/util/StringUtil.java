package com.aggregator.util;

import static com.aggregator.constants.ApplicationConstants.URL_SEPARATOR;

/**
 * Utility class for string operations.
 */
public class StringUtil {

    /**
     * Extracts the database name from a JDBC URL.
     *
     * @param jdbcUrl The JDBC URL.
     * @return The extracted database name.
     * @throws IllegalArgumentException If the JDBC URL is invalid.
     */
    public static String extractDatabaseName(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.contains(URL_SEPARATOR)) {
            throw new IllegalArgumentException("Invalid JDBC URL");
        }
        return jdbcUrl.substring(jdbcUrl.lastIndexOf(URL_SEPARATOR) + 1);
    }
}
