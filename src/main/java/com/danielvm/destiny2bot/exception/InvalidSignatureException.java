package com.danielvm.destiny2bot.exception;

import java.io.Serial;

public class InvalidSignatureException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -5638702014718290931L;

  public InvalidSignatureException(String message) {
    super(message);
  }
}
