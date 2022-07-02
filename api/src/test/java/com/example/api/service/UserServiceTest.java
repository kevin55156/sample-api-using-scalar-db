package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.UpdateUserDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class UserServiceTest {
  private static final String MOCKED_USER_ID = "mockedUserId";
  private static final String MOCKED_MOVIE_ID = "mockedMovieId";

  @Mock UserRepository userRepository;
  @Mock MovieRepository movieRepository;
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @Autowired UserService userService;

  @BeforeEach
  private void setUp() throws TransactionException {
    userService = new UserService(userRepository, movieRepository, manager);

    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createUser_shouldSuccess() throws TransactionException, InterruptedException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();
    userService.createUser(createUserDto);

    verify(tx, times(1)).commit();
  }

  @Test
  public void createUser_whenCommitFailed_shouldThrowServiceException()
      throws TransactionException, InterruptedException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();
    doThrow(CommitException.class).when(tx).commit();

    assertThrows(ServiceException.class, () -> userService.createUser(createUserDto));
    verify(tx, times(1)).abort();
  }

  @Test
  public void
      createUser_whenCommitConflictExceptionIsThrown_shouldThrowServiceExceptionAndAbordTransaction3Times()
          throws TransactionException, InterruptedException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();

    doThrow(CommitConflictException.class).when(tx).commit();

    assertThrows(ServiceException.class, () -> userService.createUser(createUserDto));
    verify(tx, times(3)).abort();
  }

  @Test
  public void updateUser_shouldSuccess() throws TransactionException, InterruptedException {
    User user = UserStub.getUser(MOCKED_USER_ID);
    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    userService.updateUser(MOCKED_USER_ID, updateUserDto);

    verify(tx, times(1)).commit();
  }

  @Test
  public void updateUser_whenCommitFailed_shouldThrowServiceException()
      throws TransactionException {
    User user = UserStub.getUser(MOCKED_USER_ID);
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);
    doThrow(CommitException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> userService.updateUser(MOCKED_USER_ID, updateUserDto));
    verify(tx, times(1)).abort();
  }

  @Test
  public void
      updateUser_whenCommitConflictException_shouldThrowServiceExceptionAndAbortTransaction3Times()
          throws Exception {
    User user = UserStub.getUser(MOCKED_USER_ID);
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);

    doThrow(CommitConflictException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> userService.updateUser(MOCKED_USER_ID, updateUserDto));
    verify(tx, times(3)).abort();
  }

  @Test
  public void deleteUser_shouldSuccess() throws TransactionException, InterruptedException {
    User user = UserStub.getUser(MOCKED_USER_ID);
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID);

    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);
    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID)).thenReturn(movie);

    userService.deleteUser(MOCKED_USER_ID);

    verify(tx, times(1)).commit();
  }

  @Test
  public void deleteUser_whenCommitFailed_shouldThrowServiceException()
      throws TransactionException {
    User user = UserStub.getUser(MOCKED_USER_ID);
    Movie movie = MovieStub.getMovie(MOCKED_MOVIE_ID);

    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);
    when(movieRepository.getMovie(tx, MOCKED_MOVIE_ID)).thenReturn(movie);

    doThrow(CommitException.class).when(tx).commit();

    assertThrows(ServiceException.class, () -> userService.deleteUser(MOCKED_USER_ID));
    verify(tx).abort();
  }

  @Test
  public void getUser_shouldSuccess() throws TransactionException, InterruptedException {
    User user = UserStub.getUser(MOCKED_USER_ID);
    when(userRepository.getUser(tx, MOCKED_USER_ID)).thenReturn(user);

    GetUserDto getUserDto = userService.getUser(MOCKED_USER_ID);

    assertEquals(getUserDto.getUserId(), user.getUserId());
    verify(userRepository).getUser(tx, MOCKED_USER_ID);
  }

  @Test
  public void listUsers_shouldSuccess() throws TransactionException, InterruptedException {
    List<User> users = new ArrayList<User>(Arrays.asList(UserStub.getUser(MOCKED_USER_ID)));

    when(userRepository.listUsers(tx)).thenReturn(users);

    List<GetUserDto> expectedUserS = userService.listUsers();

    assertEquals(expectedUserS.size(), users.size());
    assertEquals(expectedUserS.get(0).getUserId(), users.get(0).getUserId());
  }
}
