package com.example.api.cucumber;

import static com.example.api.cucumber.E2eConstants.GROUPS_ENDPOINT_URL;
import static com.example.api.cucumber.E2eConstants.GROUP_USERS;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_SINGLE_ID;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_THREE_SLASH;
import static com.example.api.cucumber.E2eConstants.STRING_FORMAT_TWO_SLASH;
import static com.example.api.cucumber.E2eConstants.USERS_ENDPOINT_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.GroupUserDto;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsStepdefs extends CucumberSpringConfiguration {
  private static E2eMethods e2eMethods = E2eMethods.getInstance();
  private String groupId;
  private String userId;
  private final HashMap<String, String> groupIds = new HashMap<>();
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

  @And("the user {string} creates the group {string}")
  public void theGroupCreated(String executionUser, String groupName) {
    CreateGroupDto createAdminGroupDto = E2eConstants.getCreateGroupDto(groupName);
    String adminGroupBody = e2eMethods.getJsonString(createAdminGroupDto);
    response = e2eMethods.post(GROUPS_ENDPOINT_URL, adminGroupBody, userIds.get(executionUser));
    groupId = response.getBody().asString();
    groupIds.putIfAbsent(groupName, groupId);
  }

  @When("the user {string} adds the user {string} into the groups {string}")
  public void theUserAddsAnotherUserIntoTheGroup(
      String executionUser, String targetUser, String groupName) {
    GroupUserDto groupUserDto = E2eConstants.getGroupUserDto(userIds.get(targetUser));
    String body = e2eMethods.getJsonString(groupUserDto);
    response =
        e2eMethods.put(
            String.format(
                STRING_FORMAT_TWO_SLASH, GROUPS_ENDPOINT_URL, groupIds.get(groupName), GROUP_USERS),
            body,
            userIds.get(executionUser));
  }

  @And("the group {string} contains the user {string}")
  public void theGroupContainsTheUser(String groupName, String targetUser) {
    // Get General Group's Group Users
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, GROUPS_ENDPOINT_URL, groupIds.get(groupName), GROUP_USERS),
            userIds.get(targetUser));
    List<GroupUserDto> groupUserList =
        e2eMethods.convertJsonStrToDataObjectList(
            response.getBody().asString(), GroupUserDto[].class);

    // assert groupUserList contains General User
    List<String> groupUserIdList = new ArrayList<String>();
    groupUserList.forEach(
        (groupUser -> {
          groupUserIdList.add(groupUser.getUserId());
        }));
    assertThat(groupUserIdList.contains(userIds.get(targetUser))).isTrue();
  }

  @And("the user {string} belongs to the group {string}")
  public void theUserBelongsToTheGroup(String targetUser, String groupName) {
    // Get General User' User Groups
    response =
        e2eMethods.getWithUserId(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            userIds.get(targetUser));
    GetUserDto user =
        e2eMethods.convertJsonStrToDataObject(response.getBody().asString(), GetUserDto.class);
    List<GetGroupDto> userGroupList = user.getUserGroups();

    // assert userGroupList contains the Group
    List<String> userGroupIdList = new ArrayList<String>();
    userGroupList.forEach(
        (userGroup -> {
          userGroupIdList.add(userGroup.getGroupId());
        }));
    assertThat(userGroupIdList.contains(groupIds.get(groupName))).isTrue();
  }

  @When("the user {string} leaves the user {string} from the group {string}")
  public void theUserLeavesGroupUserFromGroup(String userRole, String groupUser, String groupName) {
    GroupUserDto groupUserDto = E2eConstants.getGroupUserDto(userIds.get(groupUser));
    String body = e2eMethods.getJsonString(groupUserDto);
    e2eMethods.put(
        String.format(
            STRING_FORMAT_TWO_SLASH, GROUPS_ENDPOINT_URL, groupIds.get(groupName), GROUP_USERS),
        body,
        userIds.get(userRole));

    response =
        e2eMethods.deleteGroupUser(
            String.format(
                STRING_FORMAT_THREE_SLASH,
                GROUPS_ENDPOINT_URL,
                groupIds.get(groupName),
                GROUP_USERS,
                userIds.get(groupUser)),
            userIds.get(userRole));
  }

  @And("the group {string} don't contain the user {string}")
  public void theGroupNotContainsTheGroupUser(String groupName, String groupUser) {
    // Get the Group Users
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, GROUPS_ENDPOINT_URL, groupIds.get(groupName), GROUP_USERS),
            userIds.get("admin"));
    List<GroupUserDto> groupUserList =
        e2eMethods.convertJsonStrToDataObjectList(
            response.getBody().asString(), GroupUserDto[].class);

    // assert groupUserList contains the User
    List<String> groupUserIdList = new ArrayList<String>();
    groupUserList.forEach(
        (user -> {
          groupUserIdList.add(user.getUserId());
        }));
    assertThat(groupUserIdList.contains(userIds.get(groupUser))).isFalse();
  }

  @And("the user {string} don't belong to the group {string}")
  public void theUserNotBelongToTheGroup(String targetUser, String groupName) {
    // Get User Groups
    response =
        e2eMethods.getWithUserId(
            String.format(STRING_FORMAT_SINGLE_ID, USERS_ENDPOINT_URL, userIds.get(targetUser)),
            userIds.get(targetUser));
    GetUserDto user =
        e2eMethods.convertJsonStrToDataObject(response.getBody().asString(), GetUserDto.class);
    List<GetGroupDto> userGroupList = user.getUserGroups();

    // assert userGroupList contains the Group
    List<String> userGroupIdList = new ArrayList<String>();
    userGroupList.forEach(
        (userGroup -> {
          userGroupIdList.add(userGroup.getGroupId());
        }));
    assertThat(userGroupIdList.contains(groupIds.get(groupName))).isFalse();
  }

  @When("the user {string} gets the group users from the group {string}")
  public void theUserGetsTheGroupUsersFromTheGroup(String executionUser, String groupName) {
    response =
        e2eMethods.getWithUserId(
            String.format(
                STRING_FORMAT_TWO_SLASH, GROUPS_ENDPOINT_URL, groupIds.get(groupName), GROUP_USERS),
            userIds.get(executionUser));
  }

  @When("the user {string} deletes the group {string}")
  public void theUserDeleteTheGroup(String executionUser, String groupName) {
    response =
        e2eMethods.delete(
            String.format(STRING_FORMAT_SINGLE_ID, GROUPS_ENDPOINT_URL, groupIds.get(groupName)),
            userIds.get(executionUser));
  }

  @Then("it returns a status code of {int} for group")
  public void validateStatusCode(int statusCode) {
    assertThat(response.getStatusCode()).isEqualTo(statusCode);
  }
}
