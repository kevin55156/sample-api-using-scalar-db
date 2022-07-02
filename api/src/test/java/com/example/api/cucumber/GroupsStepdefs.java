package com.example.api.cucumber;

import static com.example.api.cucumber.E2eConstants.MOVIES_ENDPOINT_URL;
import static com.example.api.cucumber.E2eConstants.MOVIE_USERS;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_SINGLE_ID;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_THREE_SLASH;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_TWO_SLASH;
import static com.example.api.cucumber.E2eConstants.USERS_ENDPOINT_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.MovieUserDto;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoviesStepdefs extends CucumberSpringConfiguration {
  private static E2eMethods e2eMethods = E2eMethods.getInstance();
  private String movieId;
  private String userId;
  private final HashMap<String, String> movieIds = new HashMap<>();
  private final HashMap<String, String> userIds = new HashMap<>();
  private Response response;

  @When("the user {string} created")
  public void theUserIsCreated(String userName) {
    CreateUserDto createAdminUserDto = E2eConstants.getCreateUserDto();
    String adminUserBody = e2eMethods.getJsonString(createAdminUserDto);
    response = e2eMethods.postUser(USERS_ENDPOINT_URL, adminUserBody);
    userId = response.getBody().asString();
    userIds.putIfAbsent(userName, userId);
  }

  @And("the user {string} creates the movie {string}")
  public void theMovieCreated(String executionUser, String movieName) {
    CreateMovieDto createAdminMovieDto = E2eConstants.getCreateMovieDto(movieName);
    String adminMovieBody = e2eMethods.getJsonString(createAdminMovieDto);
    response = e2eMethods.post(MOVIES_ENDPOINT_URL, adminMovieBody, userIds.get(executionUser));
    movieId = response.getBody().asString();
    movieIds.putIfAbsent(movieName, movieId);
  }

  @When("the user {string} adds the user {string} into the movies {string}")
  public void theUserAddsAnotherUserIntoTheMovie(
      String executionUser, String targetUser, String movieName) {
    MovieUserDto movieUserDto = E2eConstants.getMovieUserDto(userIds.get(targetUser));
    String body = e2eMethods.getJsonString(movieUserDto);
    response =
        e2eMethods.put(
            String.format(
                STRING_FORMAT_TWO_SLASH, MOVIES_ENDPOINT_URL, movieIds.get(movieName), MOVIE_USERS),
            body,
            userIds.get(executionUser));
  }

  @And("the movie {string} contains the user {string}")
  public void theMovieContainsTheUser(String movieName, String targetUser) {
    // Get General Movie's Movie Users
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, MOVIES_ENDPOINT_URL, movieIds.get(movieName), MOVIE_USERS),
            userIds.get(targetUser));
    List<MovieUserDto> movieUserList =
        e2eMethods.convertJsonStrToDataObjectList(
            response.getBody().asString(), MovieUserDto[].class);

    // assert movieUserList contains General User
    List<String> movieUserIdList = new ArrayList<String>();
    movieUserList.forEach(
        (movieUser -> {
          movieUserIdList.add(movieUser.getUserId());
        }));
    assertThat(movieUserIdList.contains(userIds.get(targetUser))).isTrue();
  }

  @And("the user {string} belongs to the movie {string}")
  public void theUserBelongsToTheMovie(String targetUser, String movieName) {
    // Get General User' User Movies
    response =
        e2eMethods.getWithUserId(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            userIds.get(targetUser));
    GetUserDto user =
        e2eMethods.convertJsonStrToDataObject(response.getBody().asString(), GetUserDto.class);
    List<GetMovieDto> userMovieList = user.getUserMovies();

    // assert userMovieList contains the Movie
    List<String> userMovieIdList = new ArrayList<String>();
    userMovieList.forEach(
        (userMovie -> {
          userMovieIdList.add(userMovie.getMovieId());
        }));
    assertThat(userMovieIdList.contains(movieIds.get(movieName))).isTrue();
  }

  @When("the user {string} leaves the user {string} from the movie {string}")
  public void theUserLeavesMovieUserFromMovie(String userRole, String movieUser, String movieName) {
    MovieUserDto movieUserDto = E2eConstants.getMovieUserDto(userIds.get(movieUser));
    String body = e2eMethods.getJsonString(movieUserDto);
    e2eMethods.put(
        String.format(
            STRING_FORMAT_TWO_SLASH, MOVIES_ENDPOINT_URL, movieIds.get(movieName), MOVIE_USERS),
        body,
        userIds.get(userRole));

    response =
        e2eMethods.deleteMovieUser(
            String.format(
                STRING_FORMAT_THREE_SLASH,
                MOVIES_ENDPOINT_URL,
                movieIds.get(movieName),
                MOVIE_USERS,
                userIds.get(movieUser)),
            userIds.get(userRole));
  }

  @And("the movie {string} don't contain the user {string}")
  public void theMovieNotContainsTheMovieUser(String movieName, String movieUser) {
    // Get the Movie Users
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, MOVIES_ENDPOINT_URL, movieIds.get(movieName), MOVIE_USERS),
            userIds.get("admin"));
    List<MovieUserDto> movieUserList =
        e2eMethods.convertJsonStrToDataObjectList(
            response.getBody().asString(), MovieUserDto[].class);

    // assert movieUserList contains the User
    List<String> movieUserIdList = new ArrayList<String>();
    movieUserList.forEach(
        (user -> {
          movieUserIdList.add(user.getUserId());
        }));
    assertThat(movieUserIdList.contains(userIds.get(movieUser))).isFalse();
  }

  @And("the user {string} don't belong to the movie {string}")
  public void theUserNotBelongToTheMovie(String targetUser, String movieName) {
    // Get User Movies
    response =
        e2eMethods.getWithUserId(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            userIds.get(targetUser));
    GetUserDto user =
        e2eMethods.convertJsonStrToDataObject(response.getBody().asString(), GetUserDto.class);
    List<GetMovieDto> userMovieList = user.getUserMovies();

    // assert userMovieList contains the Movie
    List<String> userMovieIdList = new ArrayList<String>();
    userMovieList.forEach(
        (userMovie -> {
          userMovieIdList.add(userMovie.getMovieId());
        }));
    assertThat(userMovieIdList.contains(movieIds.get(movieName))).isFalse();
  }

  @When("the user {string} gets the movie users from the movie {string}")
  public void theUserGetsTheMovieUsersFromTheMovie(String executionUser, String movieName) {
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, MOVIES_ENDPOINT_URL, movieIds.get(movieName), MOVIE_USERS),
            userIds.get(executionUser));
  }

  @When("the user {string} deletes the movie {string}")
  public void theUserDeleteTheMovie(String executionUser, String movieName) {
    response =
        e2eMethods.delete(
            String.format(STRING_FORMAT_SINGLE_ID, MOVIES_ENDPOINT_URL, movieIds.get(movieName)),
            userIds.get(executionUser));
  }

  @Then("it returns a status code of {int} for movie")
  public void validateStatusCode(int statusCode) {
    assertThat(response.getStatusCode()).isEqualTo(statusCode);
  }
}
