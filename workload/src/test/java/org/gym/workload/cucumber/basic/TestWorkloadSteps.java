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

    private String request;

    private String response;

    @When("Microservice receive the request: username - {string}, year - {int}, month - {int}")
    public void microserviceReceiveTheRequestUsernameKathleenCarrYearMonth(String username, int year, int month) {

        request = "/workload?username=%s&year=%d&month=%d".formatted(username, year, month);

    }

    @Then("The workload service sends response: {string}")
    public void theWorkloadSendResponseWorkloadForKathleenCarrInMARCHIsHrsMin(String expectedResponse) {

        response = restTemplate.getForObject(request, String.class);

        assertThat(response).isEqualTo(expectedResponse);

    }

    @Given("Message with trainer workload: {string}, {string}, {string}, {string}, {int}, {string}")
    public void messageWithTrainerWorkload(String username, String firstname, String lastname, String date, int duration, String action) {

        message = new WorkloadMessage();
        message.setTrainerUsername(username);
        message.setTrainerFirstName(firstname);
        message.setTrainerLastName(lastname);
        message.setTrainingDate(Date.valueOf(date));
        message.setTrainingDuration(duration);
        message.setActionType(WorkloadMessage.ActionType.valueOf(action));

    }

    @When("Microservice receives the message")
    public void microservice_receives_the_message() {

        jmsTemplate.convertAndSend("workload-queue", message);

    }

    @Then("Send request to workload service and get {string}")
    public void the_new_trainer_with_workload_added_to_database(String expectedResponse) {

        delay();

        response = restTemplate.getForObject("/workload?username=%s&year=%d&month=%d"
                        .formatted(message.getTrainerUsername(),
                                message.getTrainingDate().toLocalDate().getYear(),
                                message.getTrainingDate().toLocalDate().getMonthValue()),
                String.class);

        assertThat(response).isEqualTo(expectedResponse);

    }

    @Given("Message with workload of existing in db trainer")
    public void messageWithWorkloadOfExistingInDbTrainerYearMonthDayDurationInMinutes() {

        message = new WorkloadMessage();
        message.setTrainerUsername("Coleman.Yates");
        message.setTrainerFirstName("Coleman");
        message.setTrainerLastName("Yates");
        message.setTrainingDuration(165); //2 hrs 45 min
        message.setTrainingDate(Date.valueOf("2025-10-01"));
        message.setActionType(WorkloadMessage.ActionType.ADD);

    }

    @When("Microservice receive the message with workload of existing in db trainer")
    public void microserviceReceiveTheMessageWithWorkloadOfExistingInDbTrainer() {

        jmsTemplate.convertAndSend("workload-queue", message);

    }


    @Then("The workload was added to database")
    public void theWorkloadAddedToDatabase() {

        delay();

        response = restTemplate.getForObject("/workload?username=%s&year=%d&month=%d"
                        .formatted(message.getTrainerUsername(),
                                message.getTrainingDate().toLocalDate().getYear(),
                                message.getTrainingDate().toLocalDate().getMonthValue()),
                String.class);

        assertThat(response).isEqualTo("Workload for Coleman.Yates in OCTOBER 2025 is 2 hrs 45 min");

    }

    @Given("Not valid message")
    public void notValidMessage() {
        message = new WorkloadMessage();
        message.setTrainerUsername("Not valid");
    }

    @When("Microservice receive the message and put it in Dead Letter Queue")
    public void microserviceReceiveTheMessageAndPutItInDeadLetterQueue() {

        jmsTemplate.convertAndSend("workload-queue", message);

    }

    @Then("Dead Letter Queue contains the message")
    public void deadLetterQueueContainsTheMessage() {

        WorkloadMessage receivedMessage = (WorkloadMessage) jmsTemplate.receiveAndConvert("ActiveMQ.DLQ");

        assertThat(receivedMessage).isNotNull();

        assertThat(receivedMessage.getTrainerUsername()).isEqualTo("Not valid");

    }

    @Given("Message with workload to delete: {string}, {string}, {string}, {string}, {int}, {string}")
    public void messageWithWorkloadToDeleteDuration(String username, String firstname, String lastname, String date, int duration, String action) {

        message = new WorkloadMessage();
        message.setTrainerUsername(username);
        message.setTrainerFirstName(firstname);
        message.setTrainerLastName(lastname);
        message.setTrainingDate(Date.valueOf(date));
        message.setTrainingDuration(duration);
        message.setActionType(WorkloadMessage.ActionType.valueOf(action));

    }

    @When("Microservice receive the message with workload to delete")
    public void microserviceReceiveTheMessageWithWorkloadToDelete() {

        jmsTemplate.convertAndSend("workload-queue", message);

    }

    @Then("Send request to workload service to check and get {string}")
    public void sendRequestToWorkloadServiceToCheckAndGet(String expectedResponse) {

        delay();

        response = restTemplate.getForObject("/workload?username=%s&year=%d&month=%d"
                        .formatted(message.getTrainerUsername(),
                                message.getTrainingDate().toLocalDate().getYear(),
                                message.getTrainingDate().toLocalDate().getMonthValue()),
                String.class);

        assertThat(response).isEqualTo(expectedResponse);

    }

    private void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
