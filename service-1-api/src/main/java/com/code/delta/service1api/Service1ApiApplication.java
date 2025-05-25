package com.code.delta.service1api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.code.delta.service1api.repository")
@EntityScan(basePackages = "com.code.delta.commondata.entity")

public class Service1ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(Service1ApiApplication.class, args);
    }

}
