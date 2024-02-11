package com.danielvm.destiny2bot.factory.creator;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dto.destiny.UserGlobalSearchBody;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ExperimentalRaidStatsCreator implements AutocompleteSource, ApplicationCommandSource {

  private final BungieClient bungieClient;

  public ExperimentalRaidStatsCreator(BungieClient bungieClient) {
    this.bungieClient = bungieClient;
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    Object focusedOption = interaction.getData().getOptions().get(0).getValue();
    return bungieClient.searchByGlobalName(new UserGlobalSearchBody((String) focusedOption), 0)
        .flatMapIterable(response -> response.getResponse().getSearchResults())
        .filter(result -> CollectionUtils.isNotEmpty(result.getDestinyMemberships()))
        .flatMap(result -> bungieClient.getGroupsForMember(3,
                result.getDestinyMemberships().get(0).membershipId(), 0, 1)
            .map(groupResponse -> {
              if (CollectionUtils.isEmpty(groupResponse.getResponse().getResults())) {
                return new Choice(result.getBungieGlobalDisplayName() + "#"
                                  + result.getBungieGlobalDisplayNameCode(),
                    result.getDestinyMemberships().get(0).membershipId());
              } else {
                String choiceName = "%s#%s [%s]".formatted(result.getBungieGlobalDisplayName(),
                    result.getBungieGlobalDisplayNameCode(),
                    groupResponse.getResponse().getResults().get(0).getGroup().getName());
                return new Choice(choiceName,
                    result.getDestinyMemberships().get(0).membershipId());
              }
            }))
        .collectList()
        .map(choices -> new InteractionResponse(
            InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType(),
            InteractionResponseData.builder()
                .choices(choices)
                .build()
        ));
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    return null;
  }
}
