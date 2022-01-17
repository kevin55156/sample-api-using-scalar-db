package com.example.api.cucumber;

import static com.example.api.cucumber.E2eConstants.GROUPS_ENDPOINT_URL;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_SINGLE_ID;
import static com.example.api.cucumber.E2eConstants.USERS_ENDPOINT_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.UpdateUserDto;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.HashMap;

public class UsersStepdefs extends CucumberSpringConfiguration {

  private static E2eMethods e2eMethods = E2eMethods.getInstance();
  private String userId;
  private final HashMap<String, String> userIds = new HashMap<>();
  private Response response;
  private final String ADMIN_GROUP = "admin";

  @When("the user {string} already existed")
  public void theUserIsCreated(String user) {
    CreateUserDto createUserDto = E2eConstants.getCreateUserDto();
    String body = e2eMethods.getJsonString(createUserDto);
    response = e2eMethods.postUser(USERS_ENDPOINT_URL, body);
    userId = response.getBody().asString();
    userIds.putIfAbsent(user, userId);
  }

  @And("the user {string} creates Admin Group")
  public void adminGroupIsCreated(String executionUser) {
    CreateGroupDto createGroupDto = E2eConstants.getCreateGroupDto(ADMIN_GROUP);
    String groupBody = e2eMethods.getJsonString(createGroupDto);
    response = e2eMethods.post(GROUPS_ENDPOINT_URL, groupBody, userIds.get(executionUser));
  }

  @When("the user {string} updates the user {string} information")
  public void theUserUpdatesUserInformation(String executionUser, String targetUser) {
    UpdateUserDto updateUserDto = E2eConstants.getUpdateUserDto();
    String body = e2eMethods.getJsonString(updateUserDto);
    response =
        e2eMethods.put(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            body,
            userIds.get(executionUser));
  }

  @When("the user {string} gets the user {string} information")
  public void theUserGetTheUserInformation(String executionUser, String targetUser) {
    response =
        e2eMethods.getWithUserId(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            userIds.get(executionUser));
  }

  @And("it returns user {string}")
  public void itReturnsUser(String targetUser) {
    GetUserDto getUserDto =
        e2eMethods.convertJsonStrToDataObject(response.getBody().asString(), GetUserDto.class);
    GetUserDto expectedGetUserDto = E2eConstants.getGetUserDto(userIds.get(targetUser));
    assertThat(getUserDto.getUserId()).isEqualTo(userIds.get(targetUser));
    assertThat(getUserDto.getEmail()).isEqualTo(expectedGetUserDto.getEmail());
    assertThat(getUserDto.getFamilyName()).isEqualTo(expectedGetUserDto.getFamilyName());
    assertThat(getUserDto.getGivenName()).isEqualTo(expectedGetUserDto.getGivenName());
    assertThat(getUserDto.getUserDetail().getPhoneNumber())
        .isEqualTo(expectedGetUserDto.getUserDetail().getPhoneNumber());
    assertThat(getUserDto.getUserDetail().getPreferredLanguage())
        .isEqualTo(expectedGetUserDto.getUserDetail().getPreferredLanguage());
  }

  @When("the user {string} gets all users")
  public void adminUserGetsAllUsers(String executionUser) {
    response = e2eMethods.getWithUserId(USERS_ENDPOINT_URL, userIds.get(executionUser));
  }

  @Then("it returns a status code of {int} for user")
  public void validateStatusCode(int statusCode) {
    assertThat(response.getStatusCode()).isEqualTo(statusCode);
  }
}
