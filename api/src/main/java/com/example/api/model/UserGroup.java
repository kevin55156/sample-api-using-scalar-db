package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = UserGroup.UserGroupBuilder.class)
public class UserGroup {
  @JsonProperty("group_id")
  String groupId;

  @JsonProperty("group_name")
  String groupName;
}
