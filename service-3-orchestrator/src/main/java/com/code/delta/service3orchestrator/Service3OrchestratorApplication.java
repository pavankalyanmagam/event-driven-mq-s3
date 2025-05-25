package com.code.delta.service3orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.code.delta.service3orchestrator.repository")
@EntityScan(basePackages = "com.code.delta.commondata.entity")

public class Service3OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(Service3OrchestratorApplication.class, args);
    }

}
