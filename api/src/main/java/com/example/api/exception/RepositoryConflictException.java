package com.example.api.exception;

public class RepositoryConflictException extends RuntimeException {
  public RepositoryConflictException(String message, Throwable e) {
    super(message, e);
  }
}
