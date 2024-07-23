package com.deahtstroke.rivenbot.exception;

import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;

public class NoCharactersFoundException extends BaseDiscordChatException {

  public NoCharactersFoundException(String message,
      InteractionResponseData interactionResponseData) {
    super(message, interactionResponseData);
  }
}
