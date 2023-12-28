package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserCharacterMessageCreator implements MessageResponseFactory {

  @Override
  public Mono<InteractionResponse> createResponse() {
    return null;
  }
}
