package com.example.api.exception;

public class UserAlreadyBelongsException extends RuntimeException {
  public UserAlreadyBelongsException(String message, Throwable e) {
    super(message, e);
  }
}
