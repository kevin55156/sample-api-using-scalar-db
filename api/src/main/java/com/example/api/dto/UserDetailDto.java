package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = UserDetailDto.UserDetailDtoBuilder.class)
public class UserDetailDto {
  @JsonProperty("phone_number")
  String phoneNumber;

  @JsonProperty("preferred_language")
  String preferredLanguage;
}
