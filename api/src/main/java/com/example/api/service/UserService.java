package com.example.api.service;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.GetUserDto.GetUserDtoBuilder;
import com.example.api.dto.UpdateUserDto;
import com.example.api.dto.UserDetailDto;
import com.example.api.exception.ObjectNotFoundException;
import com.example.api.exception.RepositoryException;
import com.example.api.exception.ServiceException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final DistributedTransactionManager db;

  @Autowired
  public UserService(
      UserRepository userRepository,
      GroupRepository groupRepository,
      DistributedTransactionManager db) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.db = db;
  }

  public String createUser(CreateUserDto createUserDto) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      String userId = UUID.randomUUID().toString();
      userRepository.createUser(tx, createUserDto, userId);
      tx.commit();
      return userId;
    } catch (CommitException
        | UnknownTransactionStatusException
        | RepositoryException
        | ObjectNotFoundException e) {
      tx.abort();
      throw new ServiceException("An error occurred when creating a User", e);
    }
  }

  public void updateUser(String userId, UpdateUserDto updateUserDto) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      List<GetGroupDto> userGroups =
          ScalarUtil.convertDataObjectListToAnotherDataObjectList(
              userRepository.getUser(tx, userId).getUserGroups(), GetGroupDto[].class);
      userRepository.updateUser(tx, updateUserDto, userGroups, userId);
      tx.commit();
    } catch (CommitException
        | UnknownTransactionStatusException
        | RepositoryException
        | ObjectNotFoundException e) {
      tx.abort();
      throw new ServiceException("An error occurred when updating a User", e);
    }
  }

  public void deleteUser(String userId) throws TransactionException {
    DistributedTransaction tx = db.start();
    try {
      User user = userRepository.getUser(tx, userId);
      List<UserGroup> userGroups = user.getUserGroups();
      if (Optional.ofNullable(userGroups).isPresent()) {
        userGroups.forEach(
            (userGroup -> {
              List<GroupUser> groupUsers =
                  groupRepository.getGroup(tx, userGroup.getGroupId()).getGroupUsers();
              groupUsers.removeIf(groupUser -> groupUser.getUserId().equals(userId));
              groupRepository.updateGroupUsers(tx, groupUsers, userGroup.getGroupId());
            }));
      }

      userRepository.deleteUser(tx, userId);
      tx.commit();
    } catch (CommitException
        | UnknownTransactionStatusException
        | RepositoryException
        | ObjectNotFoundException e) {
      tx.abort();
      throw new ServiceException("An error occurred when deleting a User", e);
    }
  }

  public GetUserDto getUser(String userId) throws TransactionException {
    DistributedTransaction tx = db.start();
    return getGetUserDto(userRepository.getUser(tx, userId));
  }

  public List<GetUserDto> listUsers() throws TransactionException {
    DistributedTransaction tx = db.start();
    List<GetUserDto> userDtoList = new ArrayList<GetUserDto>();
    List<User> userList = userRepository.listUsers(tx);

    userList.forEach(
        (user -> {
          GetUserDto userDto = getGetUserDto(user);
          userDtoList.add(userDto);
        }));
    return userDtoList;
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
        .userGroups(
            ScalarUtil.convertDataObjectListToAnotherDataObjectList(
                user.getUserGroups(), GetGroupDto[].class))
        .build();
  }
}
