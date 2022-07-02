package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = UserMovie.UserMovieBuilder.class)
public class UserMovie {
  @JsonProperty("movie_id")
  String movieId;

  @JsonProperty("movie_name")
  String movieName;
}
