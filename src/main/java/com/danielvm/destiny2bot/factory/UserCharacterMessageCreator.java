package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.service.DestinyCharacterService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserCharacterMessageCreator implements AuthorizedMessageFactory {

  private static final String CHOICE_FORMAT = "%s %s %s";
  private final DestinyCharacterService destinyCharacterService;

  public UserCharacterMessageCreator(
      DestinyCharacterService destinyCharacterService) {
    this.destinyCharacterService = destinyCharacterService;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    return destinyCharacterService.getCharactersForUser(interaction)
        .map(character -> new Choice(CHOICE_FORMAT.formatted(
            character.getCharacterClass(), character.getCharacterRace(), character.getLightLevel()),
            character.getCharacterId()))
        .collectList()
        .map(choices -> new InteractionResponse(8, InteractionResponseData.builder()
            .choices(choices).build()))
        .switchIfEmpty(Mono.empty());
  }
}
