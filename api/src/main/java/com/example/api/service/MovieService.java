package com.example.api.service;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.MovieUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.exception.ServiceException;
import com.example.api.model.Movie;
import com.example.api.model.MovieUser;
import com.example.api.model.User;
import com.example.api.model.UserMovie;
import com.example.api.repository.MovieRepository;
import com.example.api.repository.UserRepository;
import com.example.api.util.ScalarUtil;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.exception.transaction.CommitConflictException;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MovieService {
  private final MovieRepository movieRepository;
  private final UserRepository userRepository;
  private DistributedTransactionManager manager;
  private ModelMapper mapper;
  private DistributedTransaction tx;
  int retryCount = 0;

  @Autowired
  public MovieService(
      MovieRepository movieRepository,
      UserRepository userRepository,
      DistributedTransactionManager manager,
      ModelMapper mapper) {
    this.movieRepository = movieRepository;
    this.userRepository = userRepository;
    this.manager = manager;
    this.mapper = mapper;
  }

  public String createMovie(CreateMovieDto createMovieDto, String userId)
      throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when adding a movie");
        }
        TimeUnit.MILLISECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        String movieId = UUID.randomUUID().toString();
        MovieUser movieUser = MovieUser.builder().userId(userId).type("admin").build();
        List<MovieUser> movieUsers = new ArrayList<MovieUser>(Arrays.asList(movieUser));
        movieRepository.createMovie(tx, createMovieDto, movieId, movieUsers);

        User user = userRepository.getUser(tx, userId);
        List<UserMovie> userMovies =
            Optional.ofNullable(user.getUserMovies()).orElse(new ArrayList<UserMovie>());
        userMovies.add(
            UserMovie.builder().movieId(movieId).movieName(createMovieDto.getMovieName()).build());
        userRepository.updateUserMovies(tx, userId, userMovies);
        tx.commit();
        return movieId;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | ObjectAlreadyExistingException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }
    }
  }

  public void addMovieUser(String movieId, MovieUserDto movieUserDto) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when adding movieUser");
        }
        TimeUnit.SECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        Movie movie = movieRepository.getMovie(tx, movieId);
        List<MovieUser> movieUsers =
            Optional.ofNullable(movie.getMovieUsers()).orElse(new ArrayList<MovieUser>());
        movieUsers.forEach(
            (existingMovieUser -> {
              if (existingMovieUser.getUserId().equals(movieUserDto.getUserId())) {
                throw new ObjectAlreadyExistingException(this.getClass(), movieUserDto.getUserId());
              }
            }));
        movieUsers.add(
            ScalarUtil.convertDataObjectToAnotherDataObject(movieUserDto, MovieUser.class));
        movieRepository.updateMovieUsers(tx, movieUsers, movieId);

        User user = userRepository.getUser(tx, movieUserDto.getUserId());
        List<UserMovie> userMovies =
            Optional.ofNullable(user.getUserMovies()).orElse(new ArrayList<UserMovie>());
        userMovies.add(mapUserMovie(movie));
        userRepository.updateUserMovies(tx, movieUserDto.getUserId(), userMovies);
        tx.commit();
        break;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | ObjectAlreadyExistingException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movieUser", e);
      }
    }
  }

  public void deleteMovieUser(String movieId, String userId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when deleting a movieUser");
        }
        TimeUnit.SECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        Movie movie = movieRepository.getMovie(tx, movieId);
        List<MovieUser> movieUsers =
            Optional.ofNullable(movie.getMovieUsers()).orElse(new ArrayList<MovieUser>());
        List<String> movieUserIdList = new ArrayList<String>();
        movieUsers.forEach(
            (movieUser -> {
              movieUserIdList.add(movieUser.getUserId());
            }));
        if (!movieUserIdList.contains(userId)) {
          throw new ObjectNotFoundException(this.getClass(), userId);
        }

        movieUsers.removeIf(movieUser -> movieUser.getUserId().equals(userId));
        movieRepository.updateMovieUsers(tx, movieUsers, movieId);

        User user = userRepository.getUser(tx, userId);
        List<UserMovie> userMovies =
            Optional.ofNullable(user.getUserMovies()).orElse(new ArrayList<UserMovie>());
        userMovies.removeIf(userMovie -> userMovie.getMovieId().equals(movieId));
        userRepository.updateUserMovies(tx, userId, userMovies);
        tx.commit();
        break;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | ObjectNotFoundException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when deleting a movieUser", e);
      }
    }
  }

  public List<MovieUserDto> listMovieUsers(String movieId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when listing movieUsers");
        }
        TimeUnit.SECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        Movie movie = movieRepository.getMovie(tx, movieId);
        List<MovieUser> movieUsers =
            Optional.ofNullable(movie.getMovieUsers()).orElse(new ArrayList<MovieUser>());
        List<MovieUserDto> movieUserDtoList = new ArrayList<MovieUserDto>();
        movieUsers.forEach(
            (movieUser -> {
              movieUserDtoList.add(mapMovieUserDto(movieUser));
            }));
        tx.commit();
        return movieUserDtoList;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | ObjectNotFoundException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when list movieUsers", e);
      }
    }
  }

  public void deleteMovie(String movieId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when deleting a movie");
        }
        TimeUnit.SECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        Movie movie = movieRepository.getMovie(tx, movieId);
        List<MovieUser> movieUsers = movie.getMovieUsers();
        if (Optional.ofNullable(movieUsers).isPresent()) {
          movieUsers.forEach(
              (movieUser -> {
                List<UserMovie> userMovies =
                    userRepository.getUser(tx, movieUser.getUserId()).getUserMovies();
                userMovies.removeIf(userMovie -> userMovie.getMovieId().equals(movieId));
                userRepository.updateUserMovies(tx, movieUser.getUserId(), userMovies);
              }));
        }
        movieRepository.deleteMovie(tx, movieId);
        tx.commit();
        break;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | ObjectNotFoundException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when deleting a movie", e);
      }
    }
  }

  public List<GetMovieDto> listMovies() throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when listing movies");
        }
        TimeUnit.SECONDS.sleep(100);
      }

      try {
        tx = manager.start();
      } catch (TransactionException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a movie", e);
      }

      try {
        List<Movie> movieList = movieRepository.listMovies(tx);
        List<GetMovieDto> movieDtoList = new ArrayList<GetMovieDto>();
        movieList.forEach(
            (movie -> {
              GetMovieDto movieDto = mapGetMovieDto(movie);
              movieDtoList.add(movieDto);
            }));
        tx.commit();
        return movieDtoList;
      } catch (CommitConflictException
          | RepositoryConflictException
          | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when reading movies", e);
      }
    }
  }

  private UserMovie mapUserMovie(Movie movie) {
    return mapper.map(movie, UserMovie.UserMovieBuilder.class).build();
  }

  private MovieUserDto mapMovieUserDto(MovieUser movieUser) {
    return mapper.map(movieUser, MovieUserDto.MovieUserDtoBuilder.class).build();
  }

  private GetMovieDto mapGetMovieDto(Movie movie) {
    return mapper.map(movie, GetMovieDto.GetMovieDtoBuilder.class).build();
  }
}
