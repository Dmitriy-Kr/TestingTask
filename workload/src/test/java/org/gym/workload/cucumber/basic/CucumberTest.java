package org.gym.workload.cucumber.basic;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/cucumber",
        plugin = {"pretty", "html:target/cucumber/basic"},
        extraGlue = "org.gym.workload.cucumber.config")
public class CucumberTest {
}
