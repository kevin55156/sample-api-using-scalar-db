package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = CreateGroupDto.CreateGroupDtoBuilder.class)
public class CreateGroupDto {
  @JsonProperty("group_name")
  String groupName;
}
