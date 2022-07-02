Feature: Test /movies CRUD endpoints

  Background: the Admin movie and General movie already created by Admin user
    When the user "admin" created
    When the user "general" created
    And the user "admin" creates the movie "admin"
    And the user "admin" creates the movie "general"
    Then it returns a status code of 201 for movie

  Scenario: Admin user add General user into General movies
    When the user "admin" adds the user "general" into the movies "general"
    Then it returns a status code of 200 for movie
    And the movie "general" contains the user "general"
    And the user "general" belongs to the movie "general"

  Scenario: Admin user leaves General user from General movie.
    When the user "admin" leaves the user "general" from the movie "general"
    Then it returns a status code of 200 for movie
    And the movie "general" don't contain the user "general"
    And the user "general" don't belong to the movie "general"

  Scenario: Admin user get General movie's movie users 
    When the user "admin" gets the movie users from the movie "general"
    Then it returns a status code of 200 for movie

  Scenario: Admin user delete General movie
    When the user "admin" deletes the movie "general"
    Then it returns a status code of 200 for movie

  Scenario: General user delete General movie
    When the user "admin" adds the user "general" into the movies "general"
    Then it returns a status code of 200 for movie
    When the user "general" deletes the movie "general"
    Then it returns a status code of 200 for movie
    And the user "general" don't belong to the movie "general"
  


  
