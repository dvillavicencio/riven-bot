package com.danielvm.destiny2bot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

  @Serial
  private static final long serialVersionUID = -4367872183871777447L;

  public ResourceNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
