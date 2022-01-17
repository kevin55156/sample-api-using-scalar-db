package com.example.api.repository;

import com.example.api.exception.ObjectAlreadyExistingException;
import com.example.api.exception.ObjectNotFoundException;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.exception.transaction.CrudException;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

/**
 * A generic repository base class for read-only purpose
 *
 * @param <T> the data class
 */
public abstract class ScalarDbReadOnlyRepository<T> {

  public T getAndThrowsIfNotFound(DistributedTransaction tx, Get get)
      throws CrudException, ObjectNotFoundException {
    Result result =
        tx.get(get)
            .orElseThrow(
                () ->
                    new ObjectNotFoundException(this.getClass(), get.getPartitionKey().toString()));
    return this.parse(result);
  }

  public void getAndThrowsIfAlreadyExist(DistributedTransaction tx, Get get)
      throws CrudException, ObjectAlreadyExistingException {
    if (tx.get(get).isPresent()) {
      throw new ObjectAlreadyExistingException(this.getClass(), get.getPartitionKey().toString());
    }
  }

  public List<T> scan(DistributedTransaction tx, Scan scan) throws CrudException {
    List<Result> results = tx.scan(scan);
    return results.stream().map(this::parse).collect(Collectors.toList());
  }

  /**
   * Convert a Scalar DB query result to an object of the data class
   *
   * @param result the Scalar DB query result
   * @return an object of the data class
   */
  abstract T parse(@NotNull Result result);
}
