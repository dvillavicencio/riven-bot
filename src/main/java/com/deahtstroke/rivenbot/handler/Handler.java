package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

public interface Handler {

  /**
   * Serve a Discord interaction
   *
   * @param interaction the initial Discord interaction
   * @return {@link InteractionResponse}
   */
  Mono<InteractionResponse> serve(Interaction interaction);
}
