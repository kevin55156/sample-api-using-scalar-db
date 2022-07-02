package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = CreateMovieDto.CreateMovieDtoBuilder.class)
public class CreateMovieDto {
  @JsonProperty("movie_name")
  String movieName;
}
