package com.deahtstroke.rivenbot.exception;

import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDiscordChatException extends RuntimeException {

  private final String message;
  private final InteractionResponseData errorInteractionResponse;

  public BaseDiscordChatException(String message, InteractionResponseData interactionResponseData) {
    super(message);
    this.message = message;
    this.errorInteractionResponse = interactionResponseData;
  }

}
