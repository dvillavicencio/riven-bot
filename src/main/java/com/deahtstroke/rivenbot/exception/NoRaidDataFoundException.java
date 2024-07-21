package com.deahtstroke.rivenbot.exception;

public class NoRaidDataFoundException extends BaseDiscordChatException {

  public NoRaidDataFoundException(String message, String chatErrorMessage) {
    super(message, chatErrorMessage);
  }
}
