package com.example.api.repository;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.UpdateUserDto;
import com.example.api.exception.RepositoryConflictException;
import com.example.api.exception.RepositoryCrudException;
import com.example.api.model.User;
import com.example.api.model.User.UserBuilder;
import com.example.api.model.UserDetail;
import com.example.api.model.UserMovie;
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
public class UserRepository extends ScalarDbReadOnlyRepository<User> {
  public static final String NAMESPACE = "user";
  public static final String TABLE_NAME = "users";
  public static final String COMMON_KEY = "common_key";

  public String createUser(DistributedTransaction tx, CreateUserDto createUserDto, String userId) {
    try {
      Key pk = createPk(userId);
      getAndThrowsIfAlreadyExist(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(User.EMAIL, createUserDto.getEmail())
              .withValue(User.COMMON_KEY, COMMON_KEY);
      tx.put(put);
      return userId;
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Adding User failed", e);
    }
  }

  public void updateUser(
      DistributedTransaction tx,
      UpdateUserDto updateUserDto,
      List<GetMovieDto> userMovies,
      String userId)
      throws CrudException {
    try {
      Key pk = createPk(userId);
      getAndThrowsIfNotFound(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(User.EMAIL, updateUserDto.getEmail())
              .withValue(User.FAMILY_NAME, updateUserDto.getFamilyName())
              .withValue(User.GIVEN_NAME, updateUserDto.getGivenName())
              .withValue(
                  User.USER_DETAIL,
                  ScalarUtil.convertDataObjectToJsonStr(updateUserDto.getUserDetail()))
              .withValue(User.USER_MOVIES, ScalarUtil.convertDataObjectToJsonStr(userMovies))
              .withValue(User.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Updating User failed", e);
    }
  }

  public void updateUserMovies(
      DistributedTransaction tx, String userId, List<UserMovie> userMovies) {
    try {
      Key pk = createPk(userId);
      User user = getAndThrowsIfNotFound(tx, createGet(pk));
      Put put =
          new Put(pk)
              .forNamespace(NAMESPACE)
              .forTable(TABLE_NAME)
              .withValue(User.EMAIL, user.getEmail())
              .withValue(User.FAMILY_NAME, user.getFamilyName())
              .withValue(User.GIVEN_NAME, user.getGivenName())
              .withValue(
                  User.USER_DETAIL, ScalarUtil.convertDataObjectToJsonStr(user.getUserDetail()))
              .withValue(User.USER_MOVIES, ScalarUtil.convertDataObjectToJsonStr(userMovies))
              .withValue(User.COMMON_KEY, COMMON_KEY);
      tx.put(put);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Updating UserMovies failed", e);
    }
  }

  public void deleteUser(DistributedTransaction tx, String userId) {
    try {
      Key pk = createPk(userId);
      getAndThrowsIfNotFound(tx, createGet(pk));
      Delete delete = new Delete(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
      tx.delete(delete);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Deleting User failed", e);
    }
  }

  public List<User> listUsers(DistributedTransaction tx) {
    try {
      Scan scan = createScanWithCommonKey();
      return scan(tx, scan);
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Listing Users failed", e);
    }
  }

  public User getUser(DistributedTransaction tx, String userId) {
    try {
      Key pk = createPk(userId);
      return getAndThrowsIfNotFound(tx, createGet(pk));
    } catch (CrudConflictException e) {
      throw new RepositoryConflictException(e.getMessage(), e);
    } catch (CrudException e) {
      throw new RepositoryCrudException("Reading User failed", e);
    }
  }

  private Key createPk(String userId) {
    return new Key(new TextValue(User.USER_ID, userId));
  }

  private Get createGet(Key pk) {
    return new Get(pk).forNamespace(NAMESPACE).forTable(TABLE_NAME);
  }

  private Scan createScanWithCommonKey() {
    return new Scan(new Key(new TextValue(User.COMMON_KEY, COMMON_KEY)))
        .forNamespace(NAMESPACE)
        .forTable(TABLE_NAME);
  }

  @Override
  User parse(@NotNull Result result) {
    UserBuilder builder = User.builder();
    return builder
        .userId(ScalarUtil.getTextValue(result, User.USER_ID))
        .email(ScalarUtil.getTextValue(result, User.EMAIL))
        .familyName(ScalarUtil.getTextValue(result, User.FAMILY_NAME))
        .givenName(ScalarUtil.getTextValue(result, User.GIVEN_NAME))
        .userMovies(
            ScalarUtil.convertJsonStrToDataObjectList(
                ScalarUtil.getTextValue(result, User.USER_MOVIES), UserMovie[].class))
        .userDetail(
            ScalarUtil.convertJsonStrToDataObject(
                ScalarUtil.getTextValue(result, User.USER_DETAIL), UserDetail.class))
        .build();
  }
}
