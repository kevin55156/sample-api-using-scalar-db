package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = GetUserDto.GetUserDtoBuilder.class)
public class GetUserDto {
  @JsonProperty("user_id")
  String userId;

  @JsonProperty("email")
  String email;

  @JsonProperty("family_name")
  String familyName;

  @JsonProperty("given_name")
  String givenName;

  @JsonProperty("user_movies")
  List<GetMovieDto> userMovies;

  @JsonProperty("user_detail")
  UserDetailDto userDetail;
}
