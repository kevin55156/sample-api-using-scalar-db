package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.MovieUserDto;
import com.example.api.exception.ServiceException;
import com.example.api.model.Movie;
import com.example.api.model.User;
import com.example.api.repository.MovieRepository;
import com.example.api.repository.UserRepository;
import com.example.api.util.MovieStub;
import com.example.api.util.UserStub;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.exception.transaction.CommitConflictException;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.TransactionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class MovieServiceTest {
  private static final String MOCKED_MOVIE_ID_1 = "mockedMovieId";
  private static final String MOCKED_MOVIE_ID_2 = "mockedMovieId2";
  private static final String MOCKED_USER_ID_1 = "mockedUserId";
  private static final String MOCKED_USER_ID_2 = "mockedUserId2";

  @Mock UserRepository userRepository;
  @Mock MovieRepository movieRepository;
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @Autowired MovieService movieService;
  @Autowired ModelMapper mapper;

  @BeforeEach
  private void setUp() throws TransactionException {
    movieService = new MovieService(movieRepository, userRepository, manager, mapper);

    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createMovie_shouldSuccess() throws TransactionException, InterruptedException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);
    movieService.createMovie(createMovieDto, MOCKED_USER_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void createMovie_whenCommitFailed_shouldServiceException() throws TransactionException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    doThrow(CommitException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> movieService.createMovie(createMovieDto, MOCKED_USER_ID_1));
  }

  @Test
  public void
      createMovie_whenCommitConflictExceptionThrows_shouldThrowServiceExceptionAndAbortTransaction3Times()
          throws TransactionException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    doThrow(CommitConflictException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> movieService.createMovie(createMovieDto, MOCKED_USER_ID_1));
    verify(tx, times(3)).abort();
  }

  @Test
  public void addMovieUser_shouldSuccess() throws TransactionException, InterruptedException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_2);
    MovieUserDto movieUserDto = MovieStub.getMovieUserDto(MOCKED_USER_ID_2);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_2)).thenReturn(user);

    movieService.addMovieUser(MOCKED_MOVIE_ID_1, movieUserDto);

    verify(tx, times(1)).commit();
  }

  @Test
  public void addMovieUser_whenAlreadyBelongingUser_shouldThrowServiceException()
      throws TransactionException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    MovieUserDto movieUserDto = MovieStub.getMovieUserDto(MOCKED_USER_ID_1);

    Assertions.assertThrows(
        ServiceException.class, () -> movieService.addMovieUser(MOCKED_MOVIE_ID_1, movieUserDto));
    verify(tx).abort();
  }

  @Test
  public void deleteMovieUser_shouldSuccess() throws TransactionException, InterruptedException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    movieService.deleteMovieUser(MOCKED_MOVIE_ID_1, MOCKED_USER_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void deleteMovieUser_NotBelongingUser_shouldThrowServiceException()
      throws TransactionException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    Assertions.assertThrows(
        ServiceException.class,
        () -> movieService.deleteMovieUser(MOCKED_MOVIE_ID_1, MOCKED_USER_ID_2));
    verify(tx).abort();
  }

  @Test
  public void listMovieUsers_shouldSuccess() throws TransactionException, InterruptedException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    List<MovieUserDto> movieUserList = movieService.listMovieUsers(MOCKED_MOVIE_ID_1);

    assertEquals(movieUserList.size(), 1);
    assertEquals(movieUserList.get(0).getUserId(), movie.getMovieUsers().get(0).getUserId());
  }

  @Test
  public void listMovies_shouldSuccess() throws TransactionException, InterruptedException {
    Movie movie1 = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    Movie movie2 = MovieStub.getMovie(MOCKED_MOVIE_ID_2);
    List<Movie> actualMovieList = new ArrayList<Movie>(Arrays.asList(movie1, movie2));

    when(movieRepository.listMovies(tx)).thenReturn(actualMovieList);

    List<GetMovieDto> expectedMovieList = movieService.listMovies();

    assertEquals(expectedMovieList.size(), 2);
    assertEquals(expectedMovieList.get(0).getMovieId(), actualMovieList.get(0).getMovieId());
    assertEquals(expectedMovieList.get(1).getMovieId(), actualMovieList.get(1).getMovieId());
  }

  @Test
  public void deleteMovie_shouldSuccess() throws TransactionException, InterruptedException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    movieService.deleteMovie(MOCKED_MOVIE_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void deleteMovie_whenCommitFailed_shouldThrowServiceException()
      throws TransactionException {
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID_1)).thenReturn(movie);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);
    doThrow(CommitException.class).when(tx).commit();

    assertThrows(ServiceException.class, () -> movieService.deleteMovie(MOCKED_MOVIE_ID_1));
    verify(tx).abort();
  }
}
