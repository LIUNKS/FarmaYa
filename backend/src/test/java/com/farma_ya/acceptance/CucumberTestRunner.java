package com.farma_ya.acceptance;

import io.cucumber.junit.platform.engine.Cucumber;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Cucumber
@CucumberContextConfiguration
@SpringBootTest
@ContextConfiguration(classes = CucumberTestConfig.class)
public class CucumberTestRunner {
    // This class serves as a holder for the @Cucumber annotation
    // Cucumber will automatically discover and run all feature files and step
    // definitions
    // Configuration is handled via junit-platform.properties or system properties
}