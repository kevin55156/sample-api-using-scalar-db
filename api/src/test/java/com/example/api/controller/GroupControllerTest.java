package com.example.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.MovieUserDto;
import com.example.api.repository.UserRepository;
import com.example.api.security.SpringSecurityUtil.WithCustomMockUser;
import com.example.api.service.AuthenticationService;
import com.example.api.service.MovieService;
import com.example.api.util.MovieStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.api.DistributedTransactionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration
@WebMvcTest(MovieController.class)
public class MovieControllerTest {
  private static final String BASE_URL_PATH = "/movies";
  private static final String MOVIE_USERS_URL_PATH = "/movie-users";
  private static final String MOCKED_MOVIE_ID = "d8484f03-4a96-e137-12a0-25a794c4b622";
  private static final String MOCKED_USER_ID = "6695bdd7-ccb3-0468-35af-e804f79329b2";
  private static final String MOCKED_TYPE = "mockedType";
  private static final String MOCKED_MOVIE_NAME = "mockedMovieName";

  private MockMvc mockMvc;
  @MockBean private MovieService movieService;
  @MockBean private UserRepository userRepository;
  @MockBean private DistributedTransactionManager manager;
  @Autowired MovieController movieController;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private WebApplicationContext context;
  @MockBean private AuthenticationService authenticationService;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @WithCustomMockUser(role = "ROLE_USER")
  void createMovie_byGeneralUser() throws Exception {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();

    mockMvc
        .perform(
            post(BASE_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMovieDto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithCustomMockUser(movieId = MOCKED_MOVIE_ID)
  void addMovieUser_byMovieUser_shouldSuccess() throws Exception {
    MovieUserDto movieUserDto = MovieStub.getMovieUserDto(MOCKED_USER_ID);
    mockMvc
        .perform(
            put(BASE_URL_PATH + "/" + MOCKED_MOVIE_ID + MOVIE_USERS_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieUserDto)))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser
  void addMovieUser_byNonMovieUser_thenAccessDenied() throws Exception {
    MovieUserDto movieUserDto = MovieStub.getMovieUserDto(MOCKED_USER_ID);

    assertThrows(
        AccessDeniedException.class,
        () -> movieController.addMovieUsers(MOCKED_MOVIE_ID, movieUserDto));
  }

  @Test
  @WithCustomMockUser(movieId = MOCKED_MOVIE_ID)
  void deleteMovieUser_byMovieUser_shouldSuccess() throws Exception {
    mockMvc
        .perform(
            put(
                BASE_URL_PATH
                    + "/"
                    + MOCKED_MOVIE_ID
                    + MOVIE_USERS_URL_PATH
                    + "/"
                    + MOCKED_USER_ID))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser
  void deleteMovieUser_byNonMovieUser_thenAccessDenied() throws Exception {
    assertThrows(
        AccessDeniedException.class,
        () -> movieController.deleteMovieUser(MOCKED_MOVIE_ID, MOCKED_USER_ID));
  }

  @Test
  @WithCustomMockUser(movieId = MOCKED_MOVIE_ID)
  void listMovieUsers_byMovieUser_shouldSuccess() throws Exception {
    List<MovieUserDto> movieUsers =
        new ArrayList<MovieUserDto>(Arrays.asList(MovieStub.getMovieUserDto(MOCKED_USER_ID)));
    when(movieService.listMovieUsers(MOCKED_MOVIE_ID)).thenReturn(movieUsers);

    mockMvc
        .perform(get(BASE_URL_PATH + "/" + MOCKED_MOVIE_ID + MOVIE_USERS_URL_PATH))
        .andExpect(status().isOk());

    List<MovieUserDto> actualMovieUsers = movieController.listMovieUsers(MOCKED_MOVIE_ID);

    assertEquals(1, actualMovieUsers.size());
    assertEquals(MOCKED_TYPE, actualMovieUsers.get(0).getType());
  }

  @Test
  @WithCustomMockUser
  void listMovieUsers_byNonMovieUser_thenAccessDenied() throws Exception {
    assertThrows(
        AccessDeniedException.class, () -> movieController.listMovieUsers(MOCKED_MOVIE_ID));
  }

  @Test
  @WithCustomMockUser(movieId = MOCKED_MOVIE_ID)
  void deleteMovie_byMovieUser_shouldSuccess() throws Exception {
    mockMvc.perform(delete(BASE_URL_PATH + "/" + MOCKED_MOVIE_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(role = "ROLE_USER")
  void listMovies_byGeneralUser_shouldSuccess() throws Exception {
    List<GetMovieDto> movies =
        new ArrayList<GetMovieDto>(Arrays.asList(MovieStub.getGetMovieDto(MOCKED_MOVIE_ID)));
    when(movieService.listMovies()).thenReturn(movies);

    mockMvc.perform(get(BASE_URL_PATH)).andExpect(status().isOk());

    List<GetMovieDto> actualMovies = movieController.listMovies();

    assertEquals(1, actualMovies.size());
    assertEquals(MOCKED_MOVIE_NAME, actualMovies.get(0).getMovieName());
  }
}
