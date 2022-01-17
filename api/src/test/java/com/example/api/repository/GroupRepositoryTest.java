package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryException;
import com.example.api.model.Group;
import com.example.api.model.GroupUser;
import com.example.api.util.GroupStub;
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
public class GroupRepositoryTest {
  private static final String MOCKED_GROUP_ID = UUID.randomUUID().toString();
  private static final String MOCKED_USER_ID = UUID.randomUUID().toString();
  private static final String MOCKED_GROUP_NAME = "mockedGroupName";
  private static final String COLUMN_GROUP_USERS = "group_users";
  private static final String COLUMN_GROUP_NAME = "group_name";
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @MockBean Result result;
  @Autowired GroupRepository repository;

  @BeforeEach
  private void setUp() throws TransactionException {
    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createGroup_shouldSuccess() throws TransactionException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    repository.createGroup(tx, createGroupDto, MOCKED_GROUP_ID, GroupStub.getGroupUsers());

    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);
    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue groupName = new TextValue(Group.GROUP_NAME, createGroupDto.getGroupName());
    assertEquals(groupName, arg.getValues().get(COLUMN_GROUP_NAME));
    verify(tx).get(any());
  }

  @Test
  public void createGroup_groupAlreadyExists() throws TransactionException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    when(tx.get(any())).thenReturn(Optional.of(result));

    Assertions.assertThrows(
        ObjectAlreadyExistingException.class,
        () ->
            repository.createGroup(tx, createGroupDto, MOCKED_GROUP_ID, GroupStub.getGroupUsers()));
  }

  @Test
  public void createGroup_doSomeProblem_CrudException() throws TransactionException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryException.class,
        () ->
            repository.createGroup(tx, createGroupDto, MOCKED_GROUP_ID, GroupStub.getGroupUsers()));
  }

  @Test
  public void listGroups_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.scan(any())).thenReturn(Arrays.asList(result));
    List<Group> groups = repository.listGroups(tx);

    verify(tx).scan(any());
    assertEquals(MOCKED_GROUP_ID, groups.get(0).getGroupId());
    verify(tx, never()).get(any());
    verify(tx, never()).put(any(Put.class));
  }

  @Test
  public void listGroups_doSomeProblem_CrudException() throws TransactionException {
    doThrow(CrudException.class).when(tx).scan(any());
    Assertions.assertThrows(RepositoryException.class, () -> repository.listGroups(tx));
  }

  @Test
  public void getGroup_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    Group group = repository.getGroup(tx, MOCKED_GROUP_ID);

    ArgumentCaptor<Get> argumentCaptor = ArgumentCaptor.forClass(Get.class);
    verify(tx, times(1)).get(argumentCaptor.capture());

    Get arg = argumentCaptor.getValue();
    String pk = arg.getPartitionKey().get().get(0).toString();

    assertEquals(new TextValue(Group.GROUP_ID, MOCKED_GROUP_ID).toString(), pk);
    verify(tx, never()).put(any(Put.class));
    Assertions.assertNotNull(group);
  }

  @Test
  public void getGroup_groupNotFound() throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.getGroup(tx, MOCKED_GROUP_ID));
  }

  @Test
  public void getGroup_someProblem_CrudException() throws TransactionException {
    doThrow(CrudException.class).when(tx).get(any());

    Assertions.assertThrows(
        RepositoryException.class, () -> repository.getGroup(tx, MOCKED_GROUP_ID));
  }

  @Test
  public void updateGroupUsers_shouldSuccess() throws TransactionException {
    List<GroupUser> groupUsers = GroupStub.getGroupUsers();
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.updateGroupUsers(tx, groupUsers, MOCKED_GROUP_ID);

    ArgumentCaptor<Put> argumentCaptor = ArgumentCaptor.forClass(Put.class);
    verify(tx, times(1)).put(argumentCaptor.capture());

    Put arg = argumentCaptor.getValue();
    TextValue groups =
        new TextValue(Group.GROUP_USERS, ScalarUtil.convertDataObjectToJsonStr(groupUsers));
    assertEquals(groups, arg.getValues().get(COLUMN_GROUP_USERS));
    verify(tx).get(any());
  }

  @Test
  public void updateGroupUsers_groupNotFound() throws TransactionException {
    List<GroupUser> groupUsers = GroupStub.getGroupUsers();

    Assertions.assertThrows(
        ObjectNotFoundException.class,
        () -> repository.updateGroupUsers(tx, groupUsers, MOCKED_GROUP_ID));
  }

  @Test
  public void updateGroupUsers_someProblem_CrudException() throws TransactionException {
    List<GroupUser> groupUsers = GroupStub.getGroupUsers();
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).put(any(Put.class));

    Assertions.assertThrows(
        RepositoryException.class,
        () -> repository.updateGroupUsers(tx, groupUsers, MOCKED_GROUP_ID));
  }

  @Test
  public void deleteGroup_shouldSuccess() throws TransactionException {
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));
    repository.deleteGroup(tx, MOCKED_GROUP_ID);

    ArgumentCaptor<Delete> argumentCaptor = ArgumentCaptor.forClass(Delete.class);

    verify(tx, times(1)).delete(argumentCaptor.capture());
  }

  @Test
  public void deleteGroup_groupNotFound() throws TransactionException {
    Assertions.assertThrows(
        ObjectNotFoundException.class, () -> repository.deleteGroup(tx, MOCKED_GROUP_ID));
  }

  @Test
  public void deleteGroup_someProblem_CrudException() throws TransactionException {
    arrangeResult(result, MOCKED_GROUP_ID);
    when(tx.get(any())).thenReturn(Optional.of(result));

    doThrow(CrudException.class).when(tx).delete(any(Delete.class));

    Assertions.assertThrows(
        RepositoryException.class, () -> repository.deleteGroup(tx, MOCKED_GROUP_ID));
  }

  private void arrangeResult(Result result, final String groupId) {
    arrangeResultTextValue(result, Group.GROUP_ID, groupId);
    arrangeResultTextValue(result, Group.GROUP_NAME, MOCKED_GROUP_NAME);
    arrangeResultTextValue(
        result,
        Group.GROUP_USERS,
        ScalarUtil.convertDataObjectToJsonStr(
            new ArrayList<GroupUserDto>(Arrays.asList(GroupStub.getGroupUserDto(MOCKED_USER_ID)))));
  }

  private static void arrangeResultTextValue(Result result, String key, String value) {
    when(result.getValue(key)).thenReturn(Optional.of(new TextValue(value)));
  }
}
