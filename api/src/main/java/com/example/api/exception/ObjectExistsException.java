package com.example.api.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Base exception regarding object existence */
abstract class ObjectExistsException extends RuntimeException {

  private final List<String> ids = new ArrayList<>();
  private final String objectName;

  /**
   * Constructor
   *
   * @param objectClass the class of this object
   * @param id the first element of the id
   * @param ids the other elements of the id
   */
  public ObjectExistsException(Class objectClass, String id, String... ids) {
    super();
    this.objectName = objectClass.getSimpleName();
    this.ids.add(id);
    this.ids.addAll(Arrays.asList(ids));
  }

  public List<String> getIds() {
    return ids;
  }

  public String getObjectName() {
    return objectName;
  }
}
