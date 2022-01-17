package com.example.api.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Group {
  public static final String GROUP_ID = "group_id";
  public static final String GROUP_NAME = "group_name";
  public static final String GROUP_USERS = "group_users";
  public static final String COMMON_KEY = "common_key";

  String groupId;
  String groupName;
  List<GroupUser> groupUsers;
  String commonKey;
}
