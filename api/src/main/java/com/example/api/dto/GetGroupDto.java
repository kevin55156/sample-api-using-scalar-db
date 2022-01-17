package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = GetGroupDto.GetGroupDtoBuilder.class)
public class GetGroupDto {
  @JsonProperty("group_id")
  String groupId;

  @JsonProperty("group_name")
  String groupName;
}
