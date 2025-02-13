package com.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class UserAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAggregatorApplication.class, args);
    }
}
