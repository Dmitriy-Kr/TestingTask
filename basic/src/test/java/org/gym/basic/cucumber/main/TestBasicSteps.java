package org.gym.basic.cucumber.main;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.gym.basic.dto.*;
import org.gym.basic.message.WorkloadMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBasicSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    private String response;

    private String token;

    private MultiValueMap<String, String> form;

    private TrainerDto trainerDto;

    private TraineeDto traineeDto;

    @When("Trainer enters valid login and password: username - {string}, password - {string}")
    public void userEntersValidLoginAndPasswordUsernamePassword(String username, String password) {

        form = new LinkedMultiValueMap<>();
        form.set("username", username);
        form.set("password", password);

    }

    @Then("The basic service sends response with token")
    public void theBasicServiceSendsResponseWithToken() {

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "/login",
                new HttpEntity<>(form, new HttpHeaders()),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().startsWith("JWTToken:")).isTrue();

    }

    @When("Trainer enters valid data: firstname - {string}, lastname - {string}, specialization - {string}")
    public void trainerEntersValidDataFirstnameLastnameSpecialization(String firstname, String lastname, String specialization) {

        trainerDto = new TrainerDto();
        trainerDto.setFirstname("Anton");
        trainerDto.setLastname("Huge");
        trainerDto.setSpecialization("yoga");
        trainerDto.setActive(true);

    }

    @Then("The basic service sends response with username, password and token")
    public void theBasicServiceSendsResponseWithUsernamePasswordAndToken() {

        ResponseEntity<TrainerCreatedDto> responseEntity =
                restTemplate.exchange("/trainer", HttpMethod.POST, new HttpEntity<>(trainerDto), TrainerCreatedDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().getUsername()).isEqualTo("Anton.Huge");

        assertThat(responseEntity.getBody().getPassword().isEmpty()).isFalse();

        assertThat(responseEntity.getBody().getToken().isEmpty()).isFalse();

    }

    @When("Trainee enters valid data: firstname - {string}, lastname - {string}, date of birth - {string}, address - {string}")
    public void traineeEntersValidDataFirstnameLastnameDateOfBirthAddress(String firstname, String lastname, String dateOfBirth, String address) {

        traineeDto = new TraineeDto();
        traineeDto.setFirstname(firstname);
        traineeDto.setLastname(lastname);
        traineeDto.setDateOfBirth(Date.valueOf(dateOfBirth));
        traineeDto.setAddress(address);
        traineeDto.setActive(true);

    }

    @Then("The basic service sends response with Trainee username, password and token")
    public void theBasicServiceSendsResponseWithTraineeUsernamePasswordAndToken() {
        ResponseEntity<TraineeCreatedDto> responseEntity =
                restTemplate.exchange("/trainee", HttpMethod.POST, new HttpEntity<>(traineeDto), TraineeCreatedDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().getUsername()).isEqualTo("Anna.Green");

        assertThat(responseEntity.getBody().getPassword().isEmpty()).isFalse();

        assertThat(responseEntity.getBody().getToken().isEmpty()).isFalse();
    }

    @When("Trainer login: username - {string}, password - {string}")
    public void trainerLoginUsernamePassword(String username, String password) {

        form = new LinkedMultiValueMap<>();
        form.set("username", username);
        form.set("password", password);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "/login",
                new HttpEntity<>(form, new HttpHeaders()),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().startsWith("JWTToken:")).isTrue();

        token = responseEntity.getBody().substring("JWTToken: ".length());
    }

    @Then("The training is created and sent to microservice")
    public void theTrainingCreatedAndSentToMicroservice() {

        TrainingDto trainingDto = new TrainingDto();

        trainingDto.setTrainingName("box");
        trainingDto.setTrainingDay("2025-12-22");
        trainingDto.setTrainingType("fitness");
        trainingDto.setTrainingDuration(120);
        trainingDto.setTrainerUsername("Kathleen.Carr");
        trainingDto.setTraineeUsername("Dave.Batista");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(
                "/training",
                new HttpEntity<TrainingDto>(trainingDto, headers),
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Then("The microservice sends message to queue")
    public void theMicroserviceSendsMessageToQueue() {

        delay();

        WorkloadMessage receivedMessage = (WorkloadMessage) jmsTemplate.receiveAndConvert("workload-queue");

        assertThat(receivedMessage).isNotNull();

        assertThat(receivedMessage.getTrainerUsername()).isEqualTo("Kathleen.Carr");

        assertThat(receivedMessage.getActionType().name()).isEqualTo("ADD");
    }

    @When("Trainer login to app: username - {string}, password - {string}")
    public void trainerLoginToAppUsernamePassword(String username, String password) {

        form = new LinkedMultiValueMap<>();
        form.set("username", username);
        form.set("password", password);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "/login",
                new HttpEntity<>(form, new HttpHeaders()),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().startsWith("JWTToken:")).isTrue();

        token = responseEntity.getBody().substring("JWTToken: ".length());

    }

    @Then("Trainer send request to delete training by id # {int}")
    public void trainerSendRequestToDeleteTrainingById(int id) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/training/" + id,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }


    @Then("The microservice sends message to queue to delete")
    public void theMicroserviceSendsMessageToQueueToDelete() {

        delay();

        WorkloadMessage receivedMessage = (WorkloadMessage) jmsTemplate.receiveAndConvert("workload-queue");

        assertThat(receivedMessage).isNotNull();

        assertThat(receivedMessage.getTrainerUsername()).isEqualTo("Kathleen.Carr");

        assertThat(receivedMessage.getActionType().name()).isEqualTo("DELETE");

    }

    @When("Existing trainer login: username - {string}, password - {string}")
    public void whenTrainerLogin(String username, String password) {

        form = new LinkedMultiValueMap<>();
        form.set("username", username);
        form.set("password", password);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "/login",
                new HttpEntity<>(form, new HttpHeaders()),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().startsWith("JWTToken:")).isTrue();

        token = responseEntity.getBody().substring("JWTToken: ".length());

    }

    @Then("Get trainer information by username {string}")
    public void getTrainerInformation(String username) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        ResponseEntity<TrainerDto> responseEntity = restTemplate.exchange(
                "/trainer?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrainerDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.hasBody()).isTrue();

        assertThat(responseEntity.getBody().getUsername()).isEqualTo("Anton.Huge");

    }

    private void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
