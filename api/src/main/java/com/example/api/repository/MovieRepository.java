package com.example.api.repository;

import com.example.api.dto.CreateMovieDto;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.model.Movie;
import com.example.api.model.Movie.MovieBuilder;
import com.example.api.model.MovieUser;
import com.example.api.util.ScalarUtil;
import com.scalar.db.api.Delete;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.exception.transaction.CrudConflictException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import com.scalar.db.io.TextValue;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public class MovieRepository extends ScalarDbReadOnlyRepository<Movie> {
  public static final String NAMESPACE = "movie";
  public static final String TABLE_NAME = "movies";
  public static final String COMMON_KEY = "common_key";

  public void createMovie(
      DistributedTransaction tx, CreateMovieDto movie, String movieId, List<MovieUser> movieUsers) {
    try {
      Key pk = createPk(movieId);
      getAndThrowsIfAlreadyExist(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(Movie.MOVIE_NAME, movie.getMovieName())
              .withValue(Movie.MOVIE_USERS, ScalarUtil.convertDataObjectToJsonStr(movieUsers))
              .withValue(Movie.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Adding Movie failed", e);
    }
  }

  public void updateMovieUsers(
      DistributedTransaction tx, List<MovieUser> movieUsers, String movieId) {
    try {
      Key pk = createPk(movieId);
      Movie movie = getAndThrowsIfNotFound(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(Movie.MOVIE_NAME, movie.getMovieName())
              .withValue(Movie.MOVIE_USERS, ScalarUtil.convertDataObjectToJsonStr(movieUsers))
              .withValue(Movie.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Updating MovieUsers failed", e);
    }
  }

  public void deleteMovie(DistributedTransaction tx, String movieId) {
    try {
      Key pk = createPk(movieId);
      getAndThrowsIfNotFound(tx, createGet(pk));
      Delete delete = new Delete(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
      tx.delete(delete);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Deleting Movie failed", e);
    }
  }

  public List<Movie> listMovies(DistributedTransaction tx) {
    try {
      Scan scan = createScanWithCommonKey();
      return scan(tx, scan);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Reading Movies failed", e);
    }
  }

  public Movie getMovie(DistributedTransaction tx, String movieId) {
    try {
      Key pk = createPk(movieId);
      return getAndThrowsIfNotFound(tx, createGet(pk));
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Reading a Movie failed", e);
    }
  }

  private Key createPk(String movieId) {
    return new Key(new TextValue(Movie.MOVIE_ID, movieId));
  }

  private Get createGet(Key pk) {
    return new Get(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
  }

  private Scan createScanWithCommonKey() {
    return new Scan(new Key(new TextValue(Movie.COMMON_KEY, COMMON_KEY)))
        .forNamespace(NAMESPACE)
        .forTable(TABLE_NAME);
  }

  @Override
  Movie parse(@NotNull Result result) {
    MovieBuilder builder = Movie.builder();
    return builder
        .movieId(ScalarUtil.getTextValue(result, Movie.MOVIE_ID))
        .movieName(ScalarUtil.getTextValue(result, Movie.MOVIE_NAME))
        .movieUsers(
            ScalarUtil.convertJsonStrToDataObjectList(
                ScalarUtil.getTextValue(result, Movie.MOVIE_USERS), MovieUser[].class))
        .build();
  }
}
