package com.farma_ya.acceptance;

import com.farma_ya.FarmaYaApplication;
import io.cucumber.junit.platform.engine.Cucumber;
import org.springframework.boot.test.context.SpringBootTest;

@Cucumber
@SpringBootTest(classes = FarmaYaApplication.class)
public class CucumberTestRunner {
    // This class serves as a holder for the @Cucumber annotation
    // Cucumber will automatically discover and run all feature files and step
    // definitions
    // Configuration is handled via junit-platform.properties or system properties
}