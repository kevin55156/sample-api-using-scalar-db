package com.example.api.util;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.CreateGroupDto.CreateGroupDtoBuilder;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GetGroupDto.GetGroupDtoBuilder;
import com.example.api.dto.GroupUserDto;
import com.example.api.dto.GroupUserDto.GroupUserDtoBuilder;
import com.example.api.model.Group;
import com.example.api.model.Group.GroupBuilder;
import com.example.api.model.GroupUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupStub {
  private static final String MOCKED_GROUP_NAME = "mockedGroupName";
  private static final String MOCKED_USER_ID = "mockedUserId";
  private static final String MOCKED_TYPE = "mockedType";

  public static CreateGroupDto getCreateGroupDto() {
    CreateGroupDtoBuilder builder = CreateGroupDto.builder();
    return builder.groupName(MOCKED_GROUP_NAME).build();
  }

  public static GroupUserDto getGroupUserDto(String userId) {
    GroupUserDtoBuilder builder = GroupUserDto.builder();
    return builder.userId(userId).type(MOCKED_TYPE).build();
  }

  public static List<GroupUser> getGroupUsers() {
    GroupUser groupUser = GroupUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build();
    return new ArrayList<GroupUser>(Arrays.asList(groupUser));
  }

  public static Group getGroup(String groupId) {
    GroupBuilder builder = Group.builder();
    List<GroupUser> groupUsers =
        new ArrayList<GroupUser>(
            Arrays.asList(GroupUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build()));
    return builder.groupId(groupId).groupName(MOCKED_GROUP_NAME).groupUsers(groupUsers).build();
  }

  public static GetGroupDto getGetGroupDto(String groupId) {
    GetGroupDtoBuilder builder = GetGroupDto.builder();
    return builder.groupId(groupId).groupName(MOCKED_GROUP_NAME).build();
  }
}
