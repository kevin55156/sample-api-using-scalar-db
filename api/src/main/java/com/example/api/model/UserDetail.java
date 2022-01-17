package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = UserDetail.UserDetailBuilder.class)
public class UserDetail {
  @JsonProperty("preferred_language")
  String preferredLanguage;

  @JsonProperty("phone_number")
  String phoneNumber;
}
