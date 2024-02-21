package com.danielvm.destiny2bot.handler;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

/**
 * Implementations of this interface are responsible for creating message component responses that
 * are served after a Discord user has interacted with a message component, e.g., a select menu, a
 * button, etc.
 */
public interface MessageComponentSource {

  /**
   * Create an interaction response for a message component request
   *
   * @param interaction Interaction data in-case the message component response needs context
   * @return {@link InteractionResponse}
   */
  Mono<InteractionResponse> respond(Interaction interaction);
}
