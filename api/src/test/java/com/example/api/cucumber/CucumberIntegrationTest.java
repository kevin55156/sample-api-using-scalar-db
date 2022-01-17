package com.example.api.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"pretty", "html:target/cucumber-report.html"},
    glue = {"com.example.api.cucumber"},
    features = {"src/test/resources"})
public class CucumberIntegrationTest {}
