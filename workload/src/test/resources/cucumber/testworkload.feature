Feature: Test workload
  Test workload microservice functionality

  Scenario: Add trainer workload to database when trainer isn't in database
    Given Message with trainer workload: year, month, day, duration in minutes
    When Microservice receive the message
    Then The new trainer (with workload) added to database

#  Scenario: Add trainer workload to database when trainer is in database
#    Given Message with trainer workload: year, month, day, duration in minutes
#    When Microservice receive the message
#    Then The workload) added to database