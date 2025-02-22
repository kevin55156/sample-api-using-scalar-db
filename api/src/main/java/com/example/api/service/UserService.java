package com.example.api.service;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.GetUserDto.GetUserDtoBuilder;
import com.example.api.dto.UpdateUserDto;
import com.example.api.dto.UserDetailDto;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.exception.ServiceException;
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
import com.scalar.db.exception.transaction.CrudConflictException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final MovieRepository movieRepository;
  private final DistributedTransactionManager manager;
  private DistributedTransaction tx;

  int retryCount = 0;

  @Autowired
  public UserService(
      UserRepository userRepository,
      MovieRepository movieRepository,
      DistributedTransactionManager manager) {
    this.userRepository = userRepository;
    this.movieRepository = movieRepository;
    this.manager = manager;
  }

  public String createUser(CreateUserDto createUserDto) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when adding a user");
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
        String userId = UUID.randomUUID().toString();
        userRepository.createUser(tx, createUserDto, userId);
        tx.commit();
        return userId;
      } catch (CommitConflictException | RepositoryConflictException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | RepositoryCrudException | UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when adding a user", (Throwable) e);
      }
    }
  }

  public void updateUser(String userId, UpdateUserDto updateUserDto) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when updating a user");
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
        List<GetMovieDto> userMovies =
            ScalarUtil.convertDataObjectListToAnotherDataObjectList(
                userRepository.getUser(tx, userId).getUserMovies(), GetMovieDto[].class);
        userRepository.updateUser(tx, updateUserDto, userMovies, userId);
        tx.commit();
        break;
      } catch (CommitConflictException | CrudConflictException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      } catch (CommitException | CrudException | ObjectNotFoundException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        throw new ServiceException("An error occurred when updating a user", e);
      } catch (UnknownTransactionStatusException e) {
        try {
          tx.abort();
        } catch (AbortException ex) {
          log.error(ex.getMessage(), ex);
        }
        retryCount++;
      }
    }
  }

  public void deleteUser(String userId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when deleting a user");
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
        User user = userRepository.getUser(tx, userId);
        List<UserMovie> userMovies = user.getUserMovies();
        if (Optional.ofNullable(userMovies).isPresent()) {
          userMovies.forEach(
              (userMovie -> {
                List<MovieUser> movieUsers =
                    movieRepository.getMovie(tx, userMovie.getMovieId()).getMovieUsers();
                movieUsers.removeIf(movieUser -> movieUser.getUserId().equals(userId));
                movieRepository.updateMovieUsers(tx, movieUsers, userMovie.getMovieId());
              }));
        }

        userRepository.deleteUser(tx, userId);
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
        throw new ServiceException("An error occurred when deleting a user", e);
      }
    }
  }

  public GetUserDto getUser(String userId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when reading a user");
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
        GetUserDto user = getGetUserDto(userRepository.getUser(tx, userId));
        tx.commit();
        return user;
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
        throw new ServiceException("An error occurred when getting a user", e);
      }
    }
  }

  public List<GetUserDto> listUsers() throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when listing users");
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
        List<GetUserDto> userDtoList = new ArrayList<GetUserDto>();
        List<User> userList = userRepository.listUsers(tx);

        userList.forEach(
            (user -> {
              GetUserDto userDto = getGetUserDto(user);
              userDtoList.add(userDto);
            }));
        tx.commit();
        return userDtoList;
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
        throw new ServiceException("An error occurred when listing users", e);
      }
    }
  }

  private GetUserDto getGetUserDto(User user) {
    GetUserDtoBuilder builder = GetUserDto.builder();
    return builder
        .userId(user.getUserId())
        .email(user.getEmail())
        .familyName(user.getFamilyName())
        .givenName(user.getGivenName())
        .userDetail(
            ScalarUtil.convertDataObjectToAnotherDataObject(
                user.getUserDetail(), UserDetailDto.class))
        .userMovies(
            ScalarUtil.convertDataObjectListToAnotherDataObjectList(
                user.getUserMovies(), GetMovieDto[].class))
        .build();
  }
}
