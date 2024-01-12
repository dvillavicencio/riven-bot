package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

/**
 * This interface represents factories that create messages that require a level of
 * user-authentication.
 */
public interface AuthorizedMessage {

  /**
   * Create a message response using contextual interaction data as necessary
   *
   * @param userId the DiscordId of the user to verify for authorization
   * @return {@link InteractionResponse}
   */
  Mono<InteractionResponse> commandResponse(String userId);

  Mono<InteractionResponse> autocompleteResponse(String userId);
}
