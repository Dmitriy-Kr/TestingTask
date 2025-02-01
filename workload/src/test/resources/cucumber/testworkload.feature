Feature: Test workload
  Test workload microservice functionality

  Scenario: Get existing trainer workload from database
    When Microservice receive the request: username - "Kathleen.Carr", year - 2023, month - 3
    Then The workload service sends response: "Workload for Kathleen.Carr in MARCH 2023 is 0 hrs 20 min"

  Scenario Outline: Add trainer workload to database when trainer isn't exist in database
    Given Message with trainer workload: "<username>", "<firstname>", "<lastname>", "<date>", <duration>, "<action>"
    When Microservice receives the message
    Then Send request to workload service and get "<response>"

    Examples:
      | username    | firstname | lastname | date       | duration | action | response                                                 |
      | Carina.Cox  | Carina    | Cox      | 2025-12-01 | 295      | ADD    | Workload for Carina.Cox in DECEMBER 2025 is 4 hrs 55 min |

  Scenario: Add trainer workload to database when trainer is in database
    Given Message with workload of existing in db trainer
    When Microservice receive the message with workload of existing in db trainer
    Then The workload was added to database

  Scenario: Send not valid request to service
    Given Not valid message
    When Microservice receive the message and put it in Dead Letter Queue
    Then Dead Letter Queue contains the message

  Scenario Outline: Delete existing trainer workload
    Given Message with workload to delete: "<username>", "<firstname>", "<lastname>", "<date>", <duration>, "<action>"
    When Microservice receive the message with workload to delete
    Then Send request to workload service to check and get "<response>"

    Examples:
      | username       | firstname | lastname | date       | duration | action | response                                                |
      | Ward.Mejia     | Ward      | Mejia    | 2024-01-01 | 20       | DELETE | Workload for Ward.Mejia in JANUARY 2024 is 0 hrs 20 min |
      | Kathleen.Carr  | Kathleen  | Carr     | 2023-05-01 | 15       | DELETE | Workload for Kathleen.Carr in MAY 2023 is 0 hrs 30 min  |
