package com.deahtstroke.rivenbot.exception;

import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;

public class MembershipsNotFoundException extends BaseDiscordChatException {

  public MembershipsNotFoundException(String message,
      InteractionResponseData interactionResponseData) {
    super(message, interactionResponseData);
  }
}
