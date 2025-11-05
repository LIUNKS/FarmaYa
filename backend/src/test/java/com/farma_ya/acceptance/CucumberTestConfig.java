package com.farma_ya.acceptance;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class CucumberTestConfig {

    // Configuration for Cucumber tests
    // This can be extended with additional beans if needed for testing
}