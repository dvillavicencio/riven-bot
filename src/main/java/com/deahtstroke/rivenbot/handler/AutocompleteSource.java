package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

/**
 * Implementation of this interface are responsible for creating the autocomplete responses that are
 * generally used before sending a slash-command to the Discord bot
 */
public interface AutocompleteSource {

  /**
   * Create an interaction response for an autocomplete request
   *
   * @param interaction Interaction data in-case the autocomplete message source needs context
   * @return {@link InteractionResponse}
   */
  Mono<InteractionResponse> autocomplete(Interaction interaction);
}
