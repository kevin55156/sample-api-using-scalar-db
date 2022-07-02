package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = MovieUser.MovieUserBuilder.class)
public class MovieUser {
  @JsonProperty("user_id")
  String userId;

  @JsonProperty("type")
  String type;
}
