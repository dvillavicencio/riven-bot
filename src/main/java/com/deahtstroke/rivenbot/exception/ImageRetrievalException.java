package com.deahtstroke.rivenbot.exception;

import org.springframework.http.HttpStatus;

public class ImageRetrievalException extends BaseException {

  public ImageRetrievalException(String message, Throwable throwable) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
  }
}
