package org.gym.basic.cucumber.config;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TestBasic {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Container
    static ActiveMQContainer activemq =
            new ActiveMQContainer("apache/activemq-classic:5.18.3").withExposedPorts(61616);

    @DynamicPropertySource
    static void activemqProperties(DynamicPropertyRegistry registry){
        registry.add("spring.activemq.broker-url",
                () -> "tcp://%s:%d".formatted(activemq.getHost(), activemq.getMappedPort(61616)));
    }

    @BeforeAll
    public static void setup() {
        postgres.start();
        activemq.start();
    }

    @AfterAll
    public static void stop(){
        postgres.stop();
        activemq.stop();
    }
}
