package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.exception.ServiceException;
import com.example.api.model.Group;
import com.example.api.model.User;
import com.example.api.repository.GroupRepository;
import com.example.api.repository.UserRepository;
import com.example.api.util.GroupStub;
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
public class GroupServiceTest {
  private static final String MOCKED_GROUP_ID_1 = "mockedGroupId";
  private static final String MOCKED_GROUP_ID_2 = "mockedGroupId2";
  private static final String MOCKED_USER_ID_1 = "mockedUserId";
  private static final String MOCKED_USER_ID_2 = "mockedUserId2";

  @Mock UserRepository userRepository;
  @Mock GroupRepository groupRepository;
  @MockBean DistributedTransactionManager manager;
  @MockBean DistributedTransaction tx;
  @Autowired GroupService groupService;
  @Autowired ModelMapper mapper;

  @BeforeEach
  private void setUp() throws TransactionException {
    groupService = new GroupService(groupRepository, userRepository, manager, mapper);

    when(manager.start()).thenReturn(tx);
  }

  @Test
  public void createGroup_shouldSuccess() throws TransactionException, InterruptedException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);
    groupService.createGroup(createGroupDto, MOCKED_USER_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void createGroup_whenCommitFailed_shouldServiceException() throws TransactionException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    doThrow(CommitException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> groupService.createGroup(createGroupDto, MOCKED_USER_ID_1));
  }

  @Test
  public void
      createGroup_whenCommitConflictExceptionThrows_shouldThrowServiceExceptionAndAbortTransaction3Times()
          throws TransactionException {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();
    User user = UserStub.getUser(MOCKED_USER_ID_1);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    doThrow(CommitConflictException.class).when(tx).commit();

    assertThrows(
        ServiceException.class, () -> groupService.createGroup(createGroupDto, MOCKED_USER_ID_1));
    verify(tx, times(3)).abort();
  }

  @Test
  public void addGroupUser_shouldSuccess() throws TransactionException, InterruptedException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_2);
    GroupUserDto groupUserDto = GroupStub.getGroupUserDto(MOCKED_USER_ID_2);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_2)).thenReturn(user);

    groupService.addGroupUser(MOCKED_GROUP_ID_1, groupUserDto);

    verify(tx, times(1)).commit();
  }

  @Test
  public void addGroupUser_whenAlreadyBelongingUser_shouldThrowServiceException()
      throws TransactionException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    GroupUserDto groupUserDto = GroupStub.getGroupUserDto(MOCKED_USER_ID_1);

    Assertions.assertThrows(
        ServiceException.class, () -> groupService.addGroupUser(MOCKED_GROUP_ID_1, groupUserDto));
    verify(tx).abort();
  }

  @Test
  public void deleteGroupUser_shouldSuccess() throws TransactionException, InterruptedException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    groupService.deleteGroupUser(MOCKED_GROUP_ID_1, MOCKED_USER_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void deleteGroupUser_NotBelongingUser_shouldThrowServiceException()
      throws TransactionException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    Assertions.assertThrows(
        ServiceException.class,
        () -> groupService.deleteGroupUser(MOCKED_GROUP_ID_1, MOCKED_USER_ID_2));
    verify(tx).abort();
  }

  @Test
  public void listGroupUsers_shouldSuccess() throws TransactionException, InterruptedException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    List<GroupUserDto> groupUserList = groupService.listGroupUsers(MOCKED_GROUP_ID_1);

    assertEquals(groupUserList.size(), 1);
    assertEquals(groupUserList.get(0).getUserId(), group.getGroupUsers().get(0).getUserId());
  }

  @Test
  public void listGroups_shouldSuccess() throws TransactionException, InterruptedException {
    Group group1 = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    Group group2 = GroupStub.getGroup(MOCKED_GROUP_ID_2);
    List<Group> actualGroupList = new ArrayList<Group>(Arrays.asList(group1, group2));

    when(groupRepository.listGroups(tx)).thenReturn(actualGroupList);

    List<GetGroupDto> expectedGroupList = groupService.listGroups();

    assertEquals(expectedGroupList.size(), 2);
    assertEquals(expectedGroupList.get(0).getGroupId(), actualGroupList.get(0).getGroupId());
    assertEquals(expectedGroupList.get(1).getGroupId(), actualGroupList.get(1).getGroupId());
  }

  @Test
  public void deleteGroup_shouldSuccess() throws TransactionException, InterruptedException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);

    groupService.deleteGroup(MOCKED_GROUP_ID_1);

    verify(tx, times(1)).commit();
  }

  @Test
  public void deleteGroup_whenCommitFailed_shouldThrowServiceException()
      throws TransactionException {
    Group group = GroupStub.getGroup(MOCKED_GROUP_ID_1);
    User user = UserStub.getUser(MOCKED_USER_ID_1);

    when(groupRepository.getGroup(tx, MOCKED_GROUP_ID_1)).thenReturn(group);
    when(userRepository.getUser(tx, MOCKED_USER_ID_1)).thenReturn(user);
    doThrow(CommitException.class).when(tx).commit();

    assertThrows(ServiceException.class, () -> groupService.deleteGroup(MOCKED_GROUP_ID_1));
    verify(tx).abort();
  }
}
