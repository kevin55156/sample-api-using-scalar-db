package com.example.api.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Movie {
  public static final String MOVIE_ID = "movie_id";
  public static final String MOVIE_NAME = "movie_name";
  public static final String MOVIE_USERS = "movie_users";
  public static final String COMMON_KEY = "common_key";

  String movieId;
  String movieName;
  List<MovieUser> movieUsers;
  String commonKey;
}
