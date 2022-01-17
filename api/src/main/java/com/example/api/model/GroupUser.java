package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = GroupUser.GroupUserBuilder.class)
public class GroupUser {
  @JsonProperty("user_id")
  String userId;

  @JsonProperty("type")
  String type;
}
