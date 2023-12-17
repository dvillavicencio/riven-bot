package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

/**
 * Implementation of this interface are responsible for creating the human-readable message that
 * will be sent through Discord to respond to a particular slash-command
 */
public interface MessageResponseFactory {

  /**
   * Creates an Interaction Response for a slash-command
   *
   * @return {@link InteractionResponse}
   */
  Mono<InteractionResponse> createResponse();
}
