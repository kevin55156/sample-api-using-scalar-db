package com.example.api.service;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.exception.ServiceException;
import com.example.api.model.Group;
import com.example.api.model.GroupUser;
import com.example.api.model.User;
import com.example.api.model.UserGroup;
import com.example.api.repository.GroupRepository;
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
public class GroupService {
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private DistributedTransactionManager manager;
  private ModelMapper mapper;
  private DistributedTransaction tx;
  int retryCount = 0;

  @Autowired
  public GroupService(
      GroupRepository groupRepository,
      UserRepository userRepository,
      DistributedTransactionManager manager,
      ModelMapper mapper) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.manager = manager;
    this.mapper = mapper;
  }

  public String createGroup(CreateGroupDto createGroupDto, String userId)
      throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when adding a group");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

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
        throw new ServiceException("An error occurred when adding a group", e);
      }
    }
  }

  public void addGroupUser(String groupId, GroupUserDto groupUserDto) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when adding groupUser");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

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
        throw new ServiceException("An error occurred when adding a groupUser", e);
      }
    }
  }

  public void deleteGroupUser(String groupId, String userId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when deleting a groupUser");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

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
        throw new ServiceException("An error occurred when deleting a groupUser", e);
      }
    }
  }

  public List<GroupUserDto> listGroupUsers(String groupId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when listing groupUsers");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

      try {
        Group group = groupRepository.getGroup(tx, groupId);
        List<GroupUser> groupUsers =
            Optional.ofNullable(group.getGroupUsers()).orElse(new ArrayList<GroupUser>());
        List<GroupUserDto> groupUserDtoList = new ArrayList<GroupUserDto>();
        groupUsers.forEach(
            (groupUser -> {
              groupUserDtoList.add(mapGroupUserDto(groupUser));
            }));
        tx.commit();
        return groupUserDtoList;
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
        throw new ServiceException("An error occurred when list groupUsers", e);
      }
    }
  }

  public void deleteGroup(String groupId) throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when deleting a group");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

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
        throw new ServiceException("An error occurred when deleting a group", e);
      }
    }
  }

  public List<GetGroupDto> listGroups() throws InterruptedException {
    while (true) {
      if (retryCount > 0) {
        if (retryCount == 3) {
          throw new ServiceException("An error occurred when listing groups");
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
        throw new ServiceException("An error occurred when adding a group", e);
      }

      try {
        List<Group> groupList = groupRepository.listGroups(tx);
        List<GetGroupDto> groupDtoList = new ArrayList<GetGroupDto>();
        groupList.forEach(
            (group -> {
              GetGroupDto groupDto = mapGetGroupDto(group);
              groupDtoList.add(groupDto);
            }));
        tx.commit();
        return groupDtoList;
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
        throw new ServiceException("An error occurred when reading groups", e);
      }
    }
  }

  private UserGroup mapUserGroup(Group group) {
    return mapper.map(group, UserGroup.UserGroupBuilder.class).build();
  }

  private GroupUserDto mapGroupUserDto(GroupUser groupUser) {
    return mapper.map(groupUser, GroupUserDto.GroupUserDtoBuilder.class).build();
  }

  private GetGroupDto mapGetGroupDto(Group group) {
    return mapper.map(group, GetGroupDto.GetGroupDtoBuilder.class).build();
  }
}
