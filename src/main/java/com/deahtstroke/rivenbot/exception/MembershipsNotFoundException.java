package com.deahtstroke.rivenbot.exception;

public class MembershipsNotFoundException extends BaseDiscordChatException {

  public MembershipsNotFoundException(String message, String chatErrorMessage) {
    super(message, chatErrorMessage);
  }
}
