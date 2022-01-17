Feature: Test /users CRUD endpoints

  Background: Admin user and General user already existed
    When the user "general" already existed
    When the user "admin" already existed
    And the user "admin" creates Admin Group
    Then it returns a status code of 201 for user

  Scenario: Admin user updates General user information
    When the user "admin" updates the user "general" information
    Then it returns a status code of 200 for user

  Scenario: General user updates his information
    When the user "general" updates the user "general" information
    Then it returns a status code of 200 for user
  
  Scenario: Admin user gets General user information
    When the user "admin" updates the user "general" information
    When the user "admin" gets the user "general" information
    Then it returns a status code of 200 for user
    And it returns user "general"

  Scenario: General user gets his information
    When the user "general" updates the user "general" information
    When the user "admin" gets the user "general" information
    Then it returns a status code of 200 for user
    And it returns user "general"
  
  Scenario: Admin user gets all users
    When the user "admin" gets all users
    Then it returns a status code of 200 for user