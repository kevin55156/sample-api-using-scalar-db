package com.example.api.util;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.CreateMovieDto.CreateMovieDtoBuilder;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.GetMovieDto.GetMovieDtoBuilder;
import com.example.api.dto.MovieUserDto;
import com.example.api.dto.MovieUserDto.MovieUserDtoBuilder;
import com.example.api.model.Movie;
import com.example.api.model.Movie.MovieBuilder;
import com.example.api.model.MovieUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovieStub {
  private static final String MOCKED_MOVIE_NAME = "mockedMovieName";
  private static final String MOCKED_USER_ID = "mockedUserId";
  private static final String MOCKED_TYPE = "mockedType";

  public static CreateMovieDto getCreateMovieDto() {
    CreateMovieDtoBuilder builder = CreateMovieDto.builder();
    return builder.movieName(MOCKED_MOVIE_NAME).build();
  }

  public static MovieUserDto getMovieUserDto(String userId) {
    MovieUserDtoBuilder builder = MovieUserDto.builder();
    return builder.userId(userId).type(MOCKED_TYPE).build();
  }

  public static List<MovieUser> getMovieUsers() {
    MovieUser movieUser = MovieUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build();
    return new ArrayList<MovieUser>(Arrays.asList(movieUser));
  }

  public static Movie getMovie(String movieId) {
    MovieBuilder builder = Movie.builder();
    List<MovieUser> movieUsers =
        new ArrayList<MovieUser>(
            Arrays.asList(MovieUser.builder().userId(MOCKED_USER_ID).type(MOCKED_TYPE).build()));
    return builder.movieId(movieId).movieName(MOCKED_MOVIE_NAME).movieUsers(movieUsers).build();
  }

  public static GetMovieDto getGetMovieDto(String movieId) {
    GetMovieDtoBuilder builder = GetMovieDto.builder();
    return builder.movieId(movieId).movieName(MOCKED_MOVIE_NAME).build();
  }
}
