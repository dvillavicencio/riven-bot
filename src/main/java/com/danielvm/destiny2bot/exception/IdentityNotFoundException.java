package com.danielvm.destiny2bot.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class IdentityNotFoundException extends BaseException {

  @Serial
  private static final long serialVersionUID = 4099942957103917770L;

  public IdentityNotFoundException(String message) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
