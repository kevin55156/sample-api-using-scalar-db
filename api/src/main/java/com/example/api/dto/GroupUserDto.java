package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = GroupUserDto.GroupUserDtoBuilder.class)
public class GroupUserDto {
  @JsonProperty("user_id")
  String userId;

  @JsonProperty("type")
  String type;
}
