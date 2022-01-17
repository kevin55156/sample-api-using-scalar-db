package com.example.api.exception;

public class RepositoryException extends RuntimeException {
  public RepositoryException(String message, Throwable e) {
    super(message, e);
  }
}
