package com.deahtstroke.rivenbot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

  @Serial
  private static final long serialVersionUID = 7361427897341153628L;

  public BadRequestException(String message, HttpStatus status) {
    super(message, status);
  }

  public BadRequestException(String message, HttpStatus status, Throwable throwable) {
    super(message, status, throwable);
  }
}
