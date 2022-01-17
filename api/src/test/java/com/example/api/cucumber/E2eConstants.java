package com.example.api.cucumber;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.CreateGroupDto.CreateGroupDtoBuilder;
import com.example.api.dto.CreateUserDto;
import com.example.api.dto.CreateUserDto.CreateUserDtoBuilder;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GetGroupDto.GetGroupDtoBuilder;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.GetUserDto.GetUserDtoBuilder;
import com.example.api.dto.GroupUserDto;
import com.example.api.dto.GroupUserDto.GroupUserDtoBuilder;
import com.example.api.dto.UpdateUserDto;
import com.example.api.dto.UpdateUserDto.UpdateUserDtoBuilder;
import com.example.api.dto.UserDetailDto;
import com.example.api.dto.UserDetailDto.UserDetailDtoBuilder;
import com.example.api.model.GroupUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class E2eConstants {
  public static final String USERS_ENDPOINT_URL = "/users";
  public static final String GROUPS_ENDPOINT_URL = "/groups";
  public static final String GROUP_USERS = "group-users";
  public static final String STRING_FORMAT_SINGLE_ID = "%s/%s";
  public static final String STRING_FORMAT_TWO_SLASH = "%s/%s/%s";
  public static final String STRING_FORMAT_THREE_SLASH = "%s/%s/%s/%s";
  private static final String MOCKED_EMAIL = "mockedEmail";
  private static final String MOCKED_FAMILY_NAME = "mockedFamilyName";
  private static final String MOCKED_GIVEN_NAME = "mockedGivenName";
  private static final String MOCKED_PREFERRED_LANGUAGE = "mockedPreferredLanguage";
  private static final String MOCKED_PHONE_NUMBER = "mockedPhoneNumber";
  private static final String MOCKED_USER_ID = "mockedUserId";
  private static final String MOCKED_TYPE = "mockedType";

  public static CreateUserDto getCreateUserDto() {
    CreateUserDtoBuilder builder = CreateUserDto.builder();
    return builder.email(MOCKED_EMAIL).build();
  }

  public static UpdateUserDto getUpdateUserDto() {
    UpdateUserDtoBuilder builder = UpdateUserDto.builder();
    UserDetailDto userDetail = getUserDetailDto();
    return builder
        .email(MOCKED_EMAIL)
        .familyName(MOCKED_FAMILY_NAME)
        .givenName(MOCKED_GIVEN_NAME)
        .userDetail(userDetail)
        .build();
  }

  public static UserDetailDto getUserDetailDto() {
    UserDetailDtoBuilder builder = UserDetailDto.builder();
    return builder
        .preferredLanguage(MOCKED_PREFERRED_LANGUAGE)
        .phoneNumber(MOCKED_PHONE_NUMBER)
        .build();
  }

  public static GetUserDto getGetUserDto(String userId) {
    GetUserDtoBuilder builder = GetUserDto.builder();
    return builder
        .userId(userId)
        .email(MOCKED_EMAIL)
        .familyName(MOCKED_FAMILY_NAME)
        .givenName(MOCKED_GIVEN_NAME)
        .userDetail(getUserDetailDto())
        .build();
  }

  public static CreateGroupDto getCreateGroupDto(String groupName) {
    CreateGroupDtoBuilder builder = CreateGroupDto.builder();
    return builder.groupName(groupName).build();
  }

  public static GroupUserDto getGroupUserDto(String userId) {
    GroupUserDtoBuilder builder = GroupUserDto.builder();
    return builder.userId(userId).type(MOCKED_TYPE).build();
  }

  public static List<GroupUser> getGroupUsers() {
    GroupUser groupUser = GroupUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build();
    return new ArrayList<GroupUser>(Arrays.asList(groupUser));
  }

  public static GetGroupDto getGetGroupDto(String groupId, String groupName) {
    GetGroupDtoBuilder builder = GetGroupDto.builder();
    return builder.groupId(groupId).groupName(groupName).build();
  }
}
