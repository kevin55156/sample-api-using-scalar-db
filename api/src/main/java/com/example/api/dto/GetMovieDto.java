package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = GetMovieDto.GetMovieDtoBuilder.class)
public class GetMovieDto {
  @JsonProperty("movie_id")
  String movieId;

  @JsonProperty("movie_name")
  String movieName;
}
