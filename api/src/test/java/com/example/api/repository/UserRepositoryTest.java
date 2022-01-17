package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.UpdateUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryException;
import com.example.api.model.User;
import com.example.api.util.UserStub;
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
public class UserRepositoryTest {
  private static final String MOCKED_USER_ID = UUID.randomUUID().toString();
  private static final String MOCKED_EMAIL = "mockedEmail";
  private static final String MOCKED_FAMILY_NAME = "mockedFamilyName";
  private static final String MOCKED_GIVEN_NAME = "mockedGivenName";
  private static final String COLUMN_EMAIL = "email";
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @MockBean Result result;
  @Autowired UserRepository repository;

  @BeforeEach
  private void setUp() throws TransactionException {
    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createUser_shouldSuccess() throws TransactionException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();
    repository.createUser(tx, createUserDto, MOCKED_USER_ID);
    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);

    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue email = new TextValue(User.EMAIL, createUserDto.getEmail());
    assertEquals(email, arg.getValues().get(COLUMN_EMAIL));
    verify(tx).get(any());
  }

  @Test
  public void createUser_userAlreadyExists() throws TransactionException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();
    when(tx.get(any())).thenReturn(Optional.of(result));

    Assertions.assertThrows(
        ObjectAlreadyExistingException.class,
        () -> repository.createUser(tx, createUserDto, MOCKED_USER_ID));
  }

  @Test
  public void createUser_dbSomeProblems_CrudExceptionThrown() throws TransactionException {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();

    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryException.class, () -> repository.createUser(tx, createUserDto, MOCKED_USER_ID));
  }

  @Test
  public void updateUser_shouldSuccess() throws TransactionException {
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    List<GetGroupDto> groups = new ArrayList<GetGroupDto>();
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.updateUser(tx, updateUserDto, groups, MOCKED_USER_ID);

    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);

    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue email = new TextValue(User.EMAIL, updateUserDto.getEmail());

    assertEquals(email, arg.getValues().get(COLUMN_EMAIL));
    verify(tx).get(any());
  }

  @Test
  public void updateUser_userNotFounds_ObjectNotFoundExceptionThrown() throws TransactionException {
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    List<GetGroupDto> groups = new ArrayList<GetGroupDto>();

    Assertions.assertThrows(
        ObjectNotFoundException.class,
        () -> repository.updateUser(tx, updateUserDto, groups, MOCKED_USER_ID));
  }

  @Test
  public void updateUser_dbSomeProblems_CrudExceptionThrown() throws TransactionException {
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();
    List<GetGroupDto> groups = new ArrayList<GetGroupDto>();
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryException.class,
        () -> repository.updateUser(tx, updateUserDto, groups, MOCKED_USER_ID));
  }

  @Test
  public void deleteUser_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.deleteUser(tx, MOCKED_USER_ID);

    ArgumentCaptor<Delete> argumentCaptor = ArgumentCaptor.forClass(Delete.class);
    verify(tx, times(1)).delete(argumentCaptor.capture());
  }

  @Test
  public void deleteUser_userNotFounds_ObjectNotFoundExceptionThrown() throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.deleteUser(tx, MOCKED_USER_ID));
  }

  @Test
  public void deleteUser_doSomeProblem_CrudException() throws TransactionException {
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).delete(any(Delete.class));

    Assertions.assertThrows(
        RepositoryException.class, () -> repository.deleteUser(tx, MOCKED_USER_ID));
  }

  @Test
  public void getUser_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    User user = repository.getUser(tx, MOCKED_USER_ID);
    ArgumentCaptor<Get> argumentCaptor = ArgumentCaptor.forClass(Get.class);

    verify(tx, times(1)).get(argumentCaptor.capture());

    Get arg = argumentCaptor.getValue();
    String pk = arg.getPartitionKey().get().get(0).toString();

    assertEquals(new TextValue(User.USER_ID, MOCKED_USER_ID).toString(), pk);
    verify(tx, never()).put(any(Put.class));
    Assertions.assertNotNull(user);
  }

  @Test
  public void getUserByUserId_userNotFounds_ObjectNotFoundExceptionThrown()
      throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.getUser(tx, MOCKED_USER_ID));
  }

  @Test
  public void getUserByUserId_dbSomeProblems_CrudExceptionThrown() throws TransactionException {
    doThrow(CrudException.class).when(tx).get(any());

    Assertions.assertThrows(
        RepositoryException.class, () -> repository.getUser(tx, MOCKED_USER_ID));
  }

  @Test
  public void listUsers_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_USER_ID);
    when(tx.scan(any())).thenReturn(Arrays.asList(result));
    List<User> user = repository.listUsers(tx);

    verify(tx).scan(any());
    assertEquals(MOCKED_USER_ID, user.get(0).getUserId());
    verify(tx, never()).get(any());
    verify(tx, never()).put(any(Put.class));
  }

  @Test
  public void listUsers_dbSomeProblems_CrudExceptionThrown() throws TransactionException {
    doThrow(CrudException.class).when(tx).scan(any());

    Assertions.assertThrows(RepositoryException.class, () -> repository.listUsers(tx));
  }

  private void arrangeResult(Result result, final String userId) {
    arrangeResultTextValue(result, User.USER_ID, userId);
    arrangeResultTextValue(result, User.EMAIL, MOCKED_EMAIL);
    arrangeResultTextValue(result, User.FAMILY_NAME, MOCKED_FAMILY_NAME);
    arrangeResultTextValue(result, User.GIVEN_NAME, MOCKED_GIVEN_NAME);
  }

  private static void arrangeResultTextValue(Result result, String key, String value) {
    when(result.getValue(key)).thenReturn(Optional.of(new TextValue(value)));
  }
}
