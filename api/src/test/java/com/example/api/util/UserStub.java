package com.example.api.util;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.CreateUserDto.CreateUserDtoBuilder;
import com.example.api.dto.UpdateUserDto;
import com.example.api.dto.UpdateUserDto.UpdateUserDtoBuilder;
import com.example.api.dto.UserDetailDto;
import com.example.api.dto.UserDetailDto.UserDetailDtoBuilder;
import com.example.api.model.User;
import com.example.api.model.User.UserBuilder;
import com.example.api.model.UserDetail;
import com.example.api.model.UserMovie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserStub {
  private static final String MOCKED_MOVIE_ID = "mockedMovieId";
  private static final String MOCKED_EMAIL = "mockedEmail";
  private static final String MOCKED_FAMILY_NAME = "mockedFamilyName";
  private static final String MOCKED_GIVEN_NAME = "mockedGivenName";
  private static final String MOCKED_PREFERRED_LANGUAGE = "mockedPreferredLanguage";
  private static final String MOCKED_PHONE_NUMBER = "mockedPhoneNumber";
  private static final String MOCKED_MOVIE_NAME = "mockedMovieName";

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

  public static User getUser(String userId) {
    UserBuilder builder = User.builder();
    UserDetail userDetail =
        UserDetail.builder()
            .preferredLanguage(MOCKED_PREFERRED_LANGUAGE)
            .phoneNumber(MOCKED_PHONE_NUMBER)
            .build();
    List<UserMovie> userMovies =
        new ArrayList<UserMovie>(
            Arrays.asList(
                UserMovie.builder().movieId(MOCKED_MOVIE_ID).movieName(MOCKED_MOVIE_NAME).build()));

    return builder
        .userId(userId)
        .email(MOCKED_EMAIL)
        .familyName(MOCKED_FAMILY_NAME)
        .givenName(MOCKED_GIVEN_NAME)
        .userMovies(userMovies)
        .userDetail(userDetail)
        .build();
  }
}
