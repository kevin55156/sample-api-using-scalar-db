package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = MovieUserDto.MovieUserDtoBuilder.class)
public class MovieUserDto {
  @JsonProperty("user_id")
  String userId;

  @JsonProperty("type")
  String type;
}
