package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

public interface MessageService {

  /**
   * Create a discord message response based on a given interaction
   *
   * @param interaction Interaction information needed for a proper response
   * @return {@link com.danielvm.destiny2bot.dto.discord.InteractionResponse}
   */
  Mono<InteractionResponse> createResponse(Interaction interaction);
}
