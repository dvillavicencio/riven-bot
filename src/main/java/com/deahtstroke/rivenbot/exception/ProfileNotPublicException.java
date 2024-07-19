package com.deahtstroke.rivenbot.exception;

public class ProfileNotPublicException extends BaseDiscordChatException {

  public ProfileNotPublicException(String message, String chatErrorMessage) {
    super(message, chatErrorMessage);
  }
}
