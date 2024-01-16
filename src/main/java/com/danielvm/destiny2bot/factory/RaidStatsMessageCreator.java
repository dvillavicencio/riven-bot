package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.annotation.Authorized;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.service.DestinyCharacterService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RaidStatsMessageCreator implements CommandResponseCreator,
    AutocompleteResponseSourceCreator {

  private static final String CHOICE_FORMAT = "[%s] %s - %s";
  private final DestinyCharacterService destinyCharacterService;

  public RaidStatsMessageCreator(
      DestinyCharacterService destinyCharacterService) {
    this.destinyCharacterService = destinyCharacterService;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    return null;
  }

  @Override
  @Authorized
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    String userId = interaction.getMember().getUser().getId();
    return destinyCharacterService.getCharactersForUser(userId)
        .map(character -> new Choice(CHOICE_FORMAT.formatted(
            character.getLightLevel(), character.getCharacterRace(), character.getCharacterClass()),
            character.getCharacterId()))
        .collectList()
        .map(choices -> {
          if (choices.size() > 1) {
            choices.add(new Choice("All", "Gets stats for all characters"));
          }
          return new InteractionResponse(8, InteractionResponseData.builder()
              .choices(choices)
              .build());
        });
  }
}
