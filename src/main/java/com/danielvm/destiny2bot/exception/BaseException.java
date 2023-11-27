package com.danielvm.destiny2bot.exception;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BaseException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -8907382793441177291L;

  private String message;

  private HttpStatus status;

  public BaseException(String message, HttpStatus status) {
    super(message);
    this.message = message;
    this.status = status;
  }

  public BaseException(String message, HttpStatus status, Throwable throwable) {
    super(message, throwable);
    this.message = message;
    this.status = status;
  }
}
