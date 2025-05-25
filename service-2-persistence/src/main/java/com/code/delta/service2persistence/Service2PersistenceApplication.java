package com.code.delta.service2persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// Scans for JPA @Repository interfaces in the current module's repository package
@EnableJpaRepositories(basePackages = "com.code.delta.service2persistence.repository")
// Scans for @Entity classes in the common-data module's entity package
@EntityScan(basePackages = "com.code.delta.commondata.entity")

public class Service2PersistenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Service2PersistenceApplication.class, args);
    }

}
