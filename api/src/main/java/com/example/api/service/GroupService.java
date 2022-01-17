package com.example.api.service;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryException;
import com.example.api.exception.ServiceException;
import com.example.api.exception.UserAlreadyBelongsException;
import com.example.api.exception.UserNotBelongException;
import com.example.api.model.Group;
import com.example.api.model.GroupUser;
import com.example.api.model.User;
import com.example.api.model.UserGroup;
import com.example.api.repository.GroupRepository;
import com.example.api.repository.UserRepository;
import com.example.api.util.ScalarUtil;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final DistributedTransactionManager db;

  @Autowired
  public GroupService(
      GroupRepository groupRepository,
      UserRepository userRepository,
      DistributedTransactionManager db) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.db = db;
  }

  public String createGroup(CreateGroupDto createGroupDto, String userId)
      throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      String groupId = UUID.randomUUID().toString();
      GroupUser groupUser = GroupUser.builder().userId(userId).type("admin").build();
      List<GroupUser> groupUsers = new ArrayList<GroupUser>(Arrays.asList(groupUser));
      groupRepository.createGroup(tx, createGroupDto, groupId, groupUsers);

      User user = userRepository.getUser(tx, userId);
      List<UserGroup> userGroups =
          Optional.ofNullable(user.getUserGroups()).orElse(new ArrayList<UserGroup>());
      userGroups.add(
          UserGroup.builder().groupId(groupId).groupName(createGroupDto.getGroupName()).build());
      userRepository.updateUserGroups(tx, userId, userGroups);
      tx.commit();
      return groupId;
    } catch (CommitException
        | UnknownTransactionStatusException
        | RepositoryException
        | ObjectAlreadyExistingException e) {
      throw new ServiceException("An error occurred when creating a Group", e);
    }
  }

  public void addGroupUser(String groupId, GroupUserDto groupUserDto) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      Group group = groupRepository.getGroup(tx, groupId);
      List<GroupUser> groupUsers =
          Optional.ofNullable(group.getGroupUsers()).orElse(new ArrayList<GroupUser>());
      groupUsers.forEach(
          (existingGroupUser -> {
            if (existingGroupUser.getUserId().equals(groupUserDto.getUserId())) {
              throw new ObjectAlreadyExistingException(this.getClass(), groupUserDto.getUserId());
            }
          }));
      groupUsers.add(
          ScalarUtil.convertDataObjectToAnotherDataObject(groupUserDto, GroupUser.class));
      groupRepository.updateGroupUsers(tx, groupUsers, groupId);

      User user = userRepository.getUser(tx, groupUserDto.getUserId());
      List<UserGroup> userGroups =
          Optional.ofNullable(user.getUserGroups()).orElse(new ArrayList<UserGroup>());
      userGroups.add(mapUserGroup(group));
      userRepository.updateUserGroups(tx, groupUserDto.getUserId(), userGroups);
      tx.commit();
    } catch (CommitException
        | UnknownTransactionStatusException
        | RepositoryException
        | ObjectNotFoundException e) {
      tx.abort();
      throw new ServiceException("An error occurred when adding a GroupUser", e);
    } catch (ObjectAlreadyExistingException e) {
      tx.abort();
      throw new UserAlreadyBelongsException("This User already belongs to This Group", e);
    }
  }

  public void deleteGroupUser(String groupId, String userId) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      Group group = groupRepository.getGroup(tx, groupId);
      List<GroupUser> groupUsers =
          Optional.ofNullable(group.getGroupUsers()).orElse(new ArrayList<GroupUser>());
      List<String> groupUserIdList = new ArrayList<String>();
      groupUsers.forEach(
          (groupUser -> {
            groupUserIdList.add(groupUser.getUserId());
          }));
      if (!groupUserIdList.contains(userId)) {
        throw new ObjectNotFoundException(this.getClass(), userId);
      }

      groupUsers.removeIf(groupUser -> groupUser.getUserId().equals(userId));
      groupRepository.updateGroupUsers(tx, groupUsers, groupId);

      User user = userRepository.getUser(tx, userId);
      List<UserGroup> userGroups =
          Optional.ofNullable(user.getUserGroups()).orElse(new ArrayList<UserGroup>());
      userGroups.removeIf(userGroup -> userGroup.getGroupId().equals(groupId));
      userRepository.updateUserGroups(tx, userId, userGroups);
      tx.commit();
    } catch (CommitException | UnknownTransactionStatusException | RepositoryException e) {
      tx.abort();
      throw new ServiceException("An error occurred when deleting a GroupUser", e);
    } catch (ObjectNotFoundException e) {
      tx.abort();
      throw new UserNotBelongException("This User does not belong this Group", e);
    }
  }

  public List<GroupUserDto> listGroupUsers(String groupId) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      Group group = groupRepository.getGroup(tx, groupId);
      List<GroupUser> groupUsers =
          Optional.ofNullable(group.getGroupUsers()).orElse(new ArrayList<GroupUser>());
      List<GroupUserDto> groupUserDtoList = new ArrayList<GroupUserDto>();
      groupUsers.forEach(
          (groupUser -> {
            groupUserDtoList.add(mapGroupUserDto(groupUser));
          }));
      return groupUserDtoList;
    } catch (RepositoryException e) {
      throw new ServiceException("An error occurred when reading GroupUsers", e);
    }
  }

  public void deleteGroup(String groupId) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      Group group = groupRepository.getGroup(tx, groupId);
      List<GroupUser> groupUsers = group.getGroupUsers();
      if (Optional.ofNullable(groupUsers).isPresent()) {
        groupUsers.forEach(
            (groupUser -> {
              List<UserGroup> userGroups =
                  userRepository.getUser(tx, groupUser.getUserId()).getUserGroups();
              userGroups.removeIf(userGroup -> userGroup.getGroupId().equals(groupId));
              userRepository.updateUserGroups(tx, groupUser.getUserId(), userGroups);
            }));
      }
      groupRepository.deleteGroup(tx, groupId);
      tx.commit();
    } catch (CommitException | UnknownTransactionStatusException | RepositoryException e) {
      tx.abort();
      throw new ServiceException("An error occurred when deleting a Group", e);
    }
  }

  public List<GetGroupDto> listGroups() throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      List<Group> groupList = groupRepository.listGroups(tx);
      List<GetGroupDto> groupDtoList = new ArrayList<GetGroupDto>();
      groupList.forEach(
          (group -> {
            GetGroupDto groupDto = mapGetGroupDto(group);
            groupDtoList.add(groupDto);
          }));
      return groupDtoList;
    } catch (RepositoryException e) {
      throw new ServiceException("An error occurred when reading Groups", e);
    }
  }

  private UserGroup mapUserGroup(Group group) {
    return getModelMapper().map(group, UserGroup.UserGroupBuilder.class).build();
  }

  private GroupUserDto mapGroupUserDto(GroupUser groupUser) {
    return getModelMapper().map(groupUser, GroupUserDto.GroupUserDtoBuilder.class).build();
  }

  private GetGroupDto mapGetGroupDto(Group group) {
    return getModelMapper().map(group, GetGroupDto.GetGroupDtoBuilder.class).build();
  }

  private ModelMapper getModelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper
        .getConfiguration()
        .setDestinationNameTransformer(NameTransformers.builder())
        .setDestinationNamingConvention(NamingConventions.builder())
        .setMatchingStrategy(MatchingStrategies.STANDARD);
    return mapper;
  }
}
