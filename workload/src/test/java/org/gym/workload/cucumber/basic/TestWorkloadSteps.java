package org.gym.workload.cucumber.basic;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.gym.workload.message.WorkloadMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWorkloadSteps {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:5.0.10").withExposedPorts(27017);

    @Container
    ActiveMQContainer activemq = new ActiveMQContainer("apache/activemq-classic:5.18.3");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    private WorkloadMessage message;

    @Given("Message with trainer workload: year, month, day, duration in minutes")
    public void message_with_trainer_workload_year_month_day_duration_in_minutes() {

        message = new WorkloadMessage();
        message.setTrainerUsername("Carina.Cox");
        message.setTrainerFirstName("Carina");
        message.setTrainerLastName("Cox");
        message.setTrainingDuration(295);
        message.setTrainingDate(Date.valueOf("2025-12-01"));
        message.setActionType(WorkloadMessage.ActionType.ADD);
    }

    @When("Microservice receive the message")
    public void microservice_receive_the_message() {

        jmsTemplate.convertAndSend("workload-queue", message);

    }

    @Then("The new trainer \\(with workload) added to database")
    public void the_new_trainer_with_workload_added_to_database() {

        String response = restTemplate.getForObject("/workload?username=%s&year=%d&month=%d"
                .formatted(message.getTrainerUsername(),
                        message.getTrainingDate().toLocalDate().getYear(),
                        message.getTrainingDate().toLocalDate().getMonthValue()),
                String.class);

        assertThat(response).isEqualTo("Workload for Carina.Cox in DECEMBER 2025 is 4 hrs 55 min");

    }

}
