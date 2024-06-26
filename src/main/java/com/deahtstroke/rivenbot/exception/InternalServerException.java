package com.deahtstroke.rivenbot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException {

  @Serial
  private static final long serialVersionUID = -2995183695428792821L;

  public InternalServerException(String message, Throwable throwable) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
  }

  public InternalServerException(String message, HttpStatus status) {
    super(message, status);
  }

  public InternalServerException(String message, HttpStatus status, Throwable throwable) {
    super(message, status, throwable);
  }
}
