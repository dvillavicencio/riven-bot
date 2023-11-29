package com.danielvm.destiny2bot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InternalServiceException extends BaseException {

  @Serial
  private static final long serialVersionUID = -2995183695428792821L;

  public InternalServiceException(String message, HttpStatus status) {
    super(message, status);
  }
}
