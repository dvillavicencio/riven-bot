package com.deahtstroke.rivenbot.exception;

import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;

public class ProfileNotPublicException extends BaseDiscordChatException {

  public ProfileNotPublicException(String message,
      InteractionResponseData interactionResponseData) {
    super(message, interactionResponseData);
  }
}
