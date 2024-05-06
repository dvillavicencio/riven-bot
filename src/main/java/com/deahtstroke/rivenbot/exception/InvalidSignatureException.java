package com.deahtstroke.rivenbot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidSignatureException extends BaseException {

  @Serial
  private static final long serialVersionUID = -5638702014718290931L;

  public InvalidSignatureException(String message, Throwable throwable) {
    super(message, HttpStatus.BAD_REQUEST, throwable);
  }

  public InvalidSignatureException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
