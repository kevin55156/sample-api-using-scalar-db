package com.example.api.cucumber;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.CreateMovieDto.CreateMovieDtoBuilder;
import com.example.api.dto.CreateUserDto;
import com.example.api.dto.CreateUserDto.CreateUserDtoBuilder;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.GetMovieDto.GetMovieDtoBuilder;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.GetUserDto.GetUserDtoBuilder;
import com.example.api.dto.MovieUserDto;
import com.example.api.dto.MovieUserDto.MovieUserDtoBuilder;
import com.example.api.dto.UpdateUserDto;
import com.example.api.dto.UpdateUserDto.UpdateUserDtoBuilder;
import com.example.api.dto.UserDetailDto;
import com.example.api.dto.UserDetailDto.UserDetailDtoBuilder;
import com.example.api.model.MovieUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class E2eConstants {
  public static final String USERS_ENDPOINT_URL = "/users";
  public static final String MOVIES_ENDPOINT_URL = "/movies";
  public static final String MOVIE_USERS = "movie-users";
  public static final String STRING_FORMAT_SINGLE_ID = "%s/%s";
  public static final String STRING_FORMAT_TWO_SLASH = "%s/%s/%s";
  public static final String STRING_FORMAT_THREE_SLASH = "%s/%s/%s/%s";
  private static final String MOCKED_EMAIL = "mockedEmail";
  private static final String MOCKED_FAMILY_NAME = "mockedFamilyName";
  private static final String MOCKED_GIVEN_NAME = "mockedGivenName";
  private static final String MOCKED_PREFERRED_LANGUAGE = "mockedPreferredLanguage";
  private static final String MOCKED_PHONE_NUMBER = "mockedPhoneNumber";
  private static final String MOCKED_USER_ID = "mockedUserId";
  private static final String MOCKED_TYPE = "mockedType";

  public static CreateUserDto getCreateUserDto() {
    CreateUserDtoBuilder builder = CreateUserDto.builder();
    return builder.email(MOCKED_EMAIL).build();
  }

  public static UpdateUserDto getUpdateUserDto() {
    UpdateUserDtoBuilder builder = UpdateUserDto.builder();
    UserDetailDto userDetail = getUserDetailDto();
    return builder
        .email(MOCKED_EMAIL)
        .familyName(MOCKED_FAMILY_NAME)
        .givenName(MOCKED_GIVEN_NAME)
        .userDetail(userDetail)
        .build();
  }

  public static UserDetailDto getUserDetailDto() {
    UserDetailDtoBuilder builder = UserDetailDto.builder();
    return builder
        .preferredLanguage(MOCKED_PREFERRED_LANGUAGE)
        .phoneNumber(MOCKED_PHONE_NUMBER)
        .build();
  }

  public static GetUserDto getGetUserDto(String userId) {
    GetUserDtoBuilder builder = GetUserDto.builder();
    return builder
        .userId(userId)
        .email(MOCKED_EMAIL)
        .familyName(MOCKED_FAMILY_NAME)
        .givenName(MOCKED_GIVEN_NAME)
        .userDetail(getUserDetailDto())
        .build();
  }

  public static CreateMovieDto getCreateMovieDto(String movieName) {
    CreateMovieDtoBuilder builder = CreateMovieDto.builder();
    return builder.movieName(movieName).build();
  }

  public static MovieUserDto getMovieUserDto(String userId) {
    MovieUserDtoBuilder builder = MovieUserDto.builder();
    return builder.userId(userId).type(MOCKED_TYPE).build();
  }

  public static List<MovieUser> getMovieUsers() {
    MovieUser movieUser = MovieUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build();
    return new ArrayList<MovieUser>(Arrays.asList(movieUser));
  }

  public static GetMovieDto getGetMovieDto(String movieId, String movieName) {
    GetMovieDtoBuilder builder = GetMovieDto.builder();
    return builder.movieId(movieId).movieName(movieName).build();
  }
}
