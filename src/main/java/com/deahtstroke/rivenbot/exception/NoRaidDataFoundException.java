package com.deahtstroke.rivenbot.exception;

import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;

public class NoRaidDataFoundException extends BaseDiscordChatException {

  public NoRaidDataFoundException(String message,
      InteractionResponseData interactionResponseData) {
    super(message, interactionResponseData);
  }
}
