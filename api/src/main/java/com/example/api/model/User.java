package com.example.api.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
  public static final String USER_ID = "user_id";
  public static final String EMAIL = "email";
  public static final String FAMILY_NAME = "family_name";
  public static final String GIVEN_NAME = "given_name";
  public static final String USER_MOVIES = "user_movies";
  public static final String USER_DETAIL = "user_detail";
  public static final String COMMON_KEY = "common_key";

  String userId;
  String email;
  String familyName;
  String givenName;
  List<UserMovie> userMovies;
  UserDetail userDetail;
  String common_key;
}
