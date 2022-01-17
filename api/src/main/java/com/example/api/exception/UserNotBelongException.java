package com.example.api.exception;

public class UserNotBelongException extends RuntimeException {
  public UserNotBelongException(String message, Throwable e) {
    super(message, e);
  }
}
