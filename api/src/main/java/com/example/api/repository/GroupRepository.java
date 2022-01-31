package com.example.api.repository;

import com.example.api.dto.CreateGroupDto;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.model.Group;
import com.example.api.model.Group.GroupBuilder;
import com.example.api.model.GroupUser;
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
public class GroupRepository extends ScalarDbReadOnlyRepository<Group> {
  public static final String NAMESPACE = "demo";
  public static final String TABLE_NAME = "groups";
  public static final String COMMON_KEY = "common_key";

  public void createGroup(
      DistributedTransaction tx, CreateGroupDto group, String groupId, List<GroupUser> groupUsers) {
    try {
      Key pk = createPk(groupId);
      getAndThrowsIfAlreadyExist(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(Group.GROUP_NAME, group.getGroupName())
              .withValue(Group.GROUP_USERS, ScalarUtil.convertDataObjectToJsonStr(groupUsers))
              .withValue(Group.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Adding Group failed", e);
    }
  }

  public void updateGroupUsers(
      DistributedTransaction tx, List<GroupUser> groupUsers, String groupId) {
    try {
      Key pk = createPk(groupId);
      Group group = getAndThrowsIfNotFound(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(Group.GROUP_NAME, group.getGroupName())
              .withValue(Group.GROUP_USERS, ScalarUtil.convertDataObjectToJsonStr(groupUsers))
              .withValue(Group.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Updating GroupUsers failed", e);
    }
  }

  public void deleteGroup(DistributedTransaction tx, String groupId) {
    try {
      Key pk = createPk(groupId);
      getAndThrowsIfNotFound(tx, createGet(pk));
      Delete delete = new Delete(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
      tx.delete(delete);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Deleting Group failed", e);
    }
  }

  public List<Group> listGroups(DistributedTransaction tx) {
    try {
      Scan scan = createScanWithCommonKey();
      return scan(tx, scan);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Reading Groups failed", e);
    }
  }

  public Group getGroup(DistributedTransaction tx, String groupId) {
    try {
      Key pk = createPk(groupId);
      return getAndThrowsIfNotFound(tx, createGet(pk));
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Reading a Group failed", e);
    }
  }

  private Key createPk(String groupId) {
    return new Key(new TextValue(Group.GROUP_ID, groupId));
  }

  private Get createGet(Key pk) {
    return new Get(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
  }

  private Scan createScanWithCommonKey() {
    return new Scan(new Key(new TextValue(Group.COMMON_KEY, COMMON_KEY)))
        .forNamespace(NAMESPACE)
        .forTable(TABLE_NAME);
  }

  @Override
  Group parse(@NotNull Result result) {
    GroupBuilder builder = Group.builder();
    return builder
        .groupId(ScalarUtil.getTextValue(result, Group.GROUP_ID))
        .groupName(ScalarUtil.getTextValue(result, Group.GROUP_NAME))
        .groupUsers(
            ScalarUtil.convertJsonStrToDataObjectList(
                ScalarUtil.getTextValue(result, Group.GROUP_USERS), GroupUser[].class))
        .build();
  }
}
