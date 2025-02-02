Feature: Test basic
  Test basic microservice functionality

  Scenario: User login
    When Trainer enters valid login and password: username - "Kathleen.Carr", password - "7545019305"
    Then The basic service sends response with token

  Scenario: Create new Trainer
    When Trainer enters valid data: firstname - "Anton", lastname - "Huge", specialization - "yoga"
    Then The basic service sends response with username, password and token

  Scenario: Find Trainer information
    When Existing trainer login: username - "Coleman.Yates", password - "4415125129"
    Then Get trainer information by username "Anton.Huge"

  Scenario: Create new Trainee
    When Trainee enters valid data: firstname - "Anna", lastname - "Green", date of birth - "1982-10-03", address - "15 Michael Road, Springville, Verginia, 23399"
    Then The basic service sends response with Trainee username, password and token

  Scenario: Create new Training
    When Trainer login: username - "Coleman.Yates", password - "4415125129"
    Then The training is created and sent to microservice
    Then The microservice sends message to queue

  Scenario: Delete Training
    When Trainer login to app: username - "Coleman.Yates", password - "4415125129"
    Then Trainer send request to delete training by id # 8
    Then The microservice sends message to queue to delete