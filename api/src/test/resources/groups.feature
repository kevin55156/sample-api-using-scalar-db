Feature: Test /groups CRUD endpoints

  Background: the Admin group and General group already created by Admin user
    When the user "admin" created
    When the user "general" created
    And the user "admin" creates the group "admin"
    And the user "admin" creates the group "general"
    Then it returns a status code of 201 for group

  Scenario: Admin user add General user into General groups
    When the user "admin" adds the user "general" into the groups "general"
    Then it returns a status code of 200 for group
    And the group "general" contains the user "general"
    And the user "general" belongs to the group "general"

  Scenario: Admin user leaves General user from General group.
    When the user "admin" leaves the user "general" from the group "general"
    Then it returns a status code of 200 for group
    And the group "general" don't contain the user "general"
    And the user "general" don't belong to the group "general"

  Scenario: Admin user get General group's group users 
    When the user "admin" gets the group users from the group "general"
    Then it returns a status code of 200 for group

  Scenario: Admin user delete General group
    When the user "admin" deletes the group "general"
    Then it returns a status code of 200 for group

  Scenario: General user delete General group
    When the user "admin" adds the user "general" into the groups "general"
    Then it returns a status code of 200 for group
    When the user "general" deletes the group "general"
    Then it returns a status code of 200 for group
    And the user "general" don't belong to the group "general"
  


  
