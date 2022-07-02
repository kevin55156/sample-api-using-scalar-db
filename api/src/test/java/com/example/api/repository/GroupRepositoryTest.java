package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.MovieUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.model.Movie;
import com.example.api.model.MovieUser;
import com.example.api.util.MovieStub;
import com.example.api.util.ScalarUtil;
import com.scalar.db.api.Delete;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.TextValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class MovieRepositoryTest {
  private static final String MOCKED_MOVIE_ID = UUID.randomUUID().toString();
  private static final String MOCKED_USER_ID = UUID.randomUUID().toString();
  private static final String MOCKED_MOVIE_NAME = "mockedMovieName";
  private static final String COLUMN_MOVIE_USERS = "movie_users";
  private static final String COLUMN_MOVIE_NAME = "movie_name";
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @MockBean Result result;
  @Autowired MovieRepository repository;

  @BeforeEach
  private void setUp() throws TransactionException {
    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createMovie_shouldSuccess() throws TransactionException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    repository.createMovie(tx, createMovieDto, MOCKED_MOVIE_ID, MovieStub.getMovieUsers());

    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);
    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue movieName = new TextValue(Movie.MOVIE_NAME, createMovieDto.getMovieName());
    assertEquals(movieName, arg.getValues().get(COLUMN_MOVIE_NAME));
    verify(tx).get(any());
  }

  @Test
  public void createMovie_movieAlreadyExists() throws TransactionException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    when(tx.get(any())).thenReturn(Optional.of(result));

    Assertions.assertThrows(
        ObjectAlreadyExistingException.class,
        () ->
            repository.createMovie(tx, createMovieDto, MOCKED_MOVIE_ID, MovieStub.getMovieUsers()));
  }

  @Test
  public void createMovie_doSomeProblem_CrudException() throws TransactionException {
    CreateMovieDto createMovieDto = MovieStub.getCreateMovieDto();
    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryCrudException.class,
        () ->
            repository.createMovie(tx, createMovieDto, MOCKED_MOVIE_ID, MovieStub.getMovieUsers()));
  }

  @Test
  public void listMovies_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.scan(any())).thenReturn(Arrays.asList(result));
    List<Movie> movies = repository.listMovies(tx);

    verify(tx).scan(any());
    assertEquals(MOCKED_MOVIE_ID, movies.get(0).getMovieId());
    verify(tx, never()).get(any());
    verify(tx, never()).put(any(Put.class));
  }

  @Test
  public void listMovies_doSomeProblem_CrudException() throws TransactionException {
    doThrow(CrudException.class).when(tx).scan(any());
    Assertions.assertThrows(RepositoryCrudException.class, () -> repository.listMovies(tx));
  }

  @Test
  public void getMovie_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    Movie movie = repository.getMovie(tx, MOCKED_MOVIE_ID);

    ArgumentCaptor<Get> argumentCaptor = ArgumentCaptor.forClass(Get.class);
    verify(tx, times(1)).get(argumentCaptor.capture());

    Get arg = argumentCaptor.getValue();
    String pk = arg.getPartitionKey().get().get(0).toString();

    assertEquals(new TextValue(Movie.MOVIE_ID, MOCKED_MOVIE_ID).toString(), pk);
    verify(tx, never()).put(any(Put.class));
    Assertions.assertNotNull(movie);
  }

  @Test
  public void getMovie_movieNotFound() throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.getMovie(tx, MOCKED_MOVIE_ID));
  }

  @Test
  public void getMovie_someProblem_CrudException() throws TransactionException {
    doThrow(CrudException.class).when(tx).get(any());

    Assertions.assertThrows(
        RepositoryCrudException.class, () -> repository.getMovie(tx, MOCKED_MOVIE_ID));
  }

  @Test
  public void updateMovieUsers_shouldSuccess() throws TransactionException {
    List<MovieUser> movieUsers = MovieStub.getMovieUsers();
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.updateMovieUsers(tx, movieUsers, MOCKED_MOVIE_ID);

    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);
    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue movies =
        new TextValue(Movie.MOVIE_USERS, ScalarUtil.convertDataObjectToJsonStr(movieUsers));
    assertEquals(movies, arg.getValues().get(COLUMN_MOVIE_USERS));
    verify(tx).get(any());
  }

  @Test
  public void updateMovieUsers_movieNotFound() throws TransactionException {
    List<MovieUser> movieUsers = MovieStub.getMovieUsers();

    Assertions.assertThrows(
        ObjectNotFoundException.class,
        () -> repository.updateMovieUsers(tx, movieUsers, MOCKED_MOVIE_ID));
  }

  @Test
  public void updateMovieUsers_someProblem_CrudException() throws TransactionException {
    List<MovieUser> movieUsers = MovieStub.getMovieUsers();
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryCrudException.class,
        () -> repository.updateMovieUsers(tx, movieUsers, MOCKED_MOVIE_ID));
  }

  @Test
  public void deleteMovie_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.deleteMovie(tx, MOCKED_MOVIE_ID);

    ArgumentCaptor<Delete> argumentCaptor = ArgumentCaptor.forClass(Delete.class);

    verify(tx, times(1)).delete(argumentCaptor.capture());
  }

  @Test
  public void deleteMovie_movieNotFound() throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.deleteMovie(tx, MOCKED_MOVIE_ID));
  }

  @Test
  public void deleteMovie_someProblem_CrudException() throws TransactionException {
    arrangeResult(result, MOCKED_MOVIE_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).delete(any(Delete.class));

    Assertions.assertThrows(
        RepositoryCrudException.class, () -> repository.deleteMovie(tx, MOCKED_MOVIE_ID));
  }

  private void arrangeResult(Result result, final String movieId) {
    arrangeResultTextValue(result, Movie.MOVIE_ID, movieId);
    arrangeResultTextValue(result, Movie.MOVIE_NAME, MOCKED_MOVIE_NAME);
    arrangeResultTextValue(
        result,
        Movie.MOVIE_USERS,
        ScalarUtil.convertDataObjectToJsonStr(
            new ArrayList<MovieUserDto>(Arrays.asList(MovieStub.getMovieUserDto(MOCKED_USER_ID)))));
  }

  private static void arrangeResultTextValue(Result result, String key, String value) {
    when(result.getValue(key)).thenReturn(Optional.of(new TextValue(value)));
  }
}
