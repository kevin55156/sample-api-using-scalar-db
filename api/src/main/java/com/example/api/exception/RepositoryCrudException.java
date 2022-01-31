package com.example.api.exception;

public class RepositoryCrudException extends RuntimeException {
  public RepositoryCrudException(String message, Throwable e) {
    super(message, e);
  }
}
