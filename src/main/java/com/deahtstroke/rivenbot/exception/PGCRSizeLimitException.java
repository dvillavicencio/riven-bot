package com.deahtstroke.rivenbot.exception;

import lombok.Getter;

@Getter
public class PGCRSizeLimitException extends RuntimeException {

  private String message;

  public PGCRSizeLimitException(String message) {
    super(message);
    this.message = message;
  }

}
