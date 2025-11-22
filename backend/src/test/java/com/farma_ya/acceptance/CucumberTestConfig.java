package com.farma_ya.acceptance;

import com.farma_ya.FarmaYaApplication;
import com.farma_ya.service.OrderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FarmaYaApplication.class)
@ComponentScan(basePackages = "com.farma_ya")
public class CucumberTestConfig {

    // Configuration for Cucumber tests
    // This can be extended with additional beans if needed for testing
}