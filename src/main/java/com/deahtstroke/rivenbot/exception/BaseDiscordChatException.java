package com.deahtstroke.rivenbot.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDiscordChatException extends RuntimeException {

  private String message;
  private String chatErrorMessage;

  public BaseDiscordChatException(String message, String chatErrorMessage) {
    super(message);
    this.message = message;
    this.chatErrorMessage = chatErrorMessage;
  }

  public BaseDiscordChatException(String message, String chatErrorMessage, Throwable throwable) {
    super(message, throwable);
    this.message = message;
    this.chatErrorMessage = chatErrorMessage;
  }

}
