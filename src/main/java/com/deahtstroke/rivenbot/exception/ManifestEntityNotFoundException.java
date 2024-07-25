package com.deahtstroke.rivenbot.exception;

import org.springframework.http.HttpStatus;

public class ManifestEntityNotFoundException extends BaseException {

  public ManifestEntityNotFoundException(String message, HttpStatus status) {
    super(message, status);
  }
}
