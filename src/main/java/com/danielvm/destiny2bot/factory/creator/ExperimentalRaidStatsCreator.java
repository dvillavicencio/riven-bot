package com.danielvm.destiny2bot.factory.creator;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.DiscordClient;
import com.danielvm.destiny2bot.dto.destiny.UserGlobalSearchBody;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.EmbeddedField;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import com.danielvm.destiny2bot.service.RaidStatsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class ExperimentalRaidStatsCreator implements AutocompleteSource, ApplicationCommandSource {

  private final BungieClient defaultBungieClient;
  private final DiscordClient discordClient;
  private final RaidStatsService raidStatsService;
  private final ObjectMapper objectMapper;

  public ExperimentalRaidStatsCreator(
      BungieClient defaultBungieClient,
      DiscordClient discordClient,
      RaidStatsService raidStatsService,
      ObjectMapper objectMapper) {
    this.defaultBungieClient = defaultBungieClient;
    this.discordClient = discordClient;
    this.raidStatsService = raidStatsService;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    Object focusedOption = interaction.getData().getOptions().get(0).getValue();
    return defaultBungieClient.searchByGlobalName(new UserGlobalSearchBody((String) focusedOption),
            0)
        .flatMapIterable(response -> response.getResponse().getSearchResults())
        .filter(result -> CollectionUtils.isNotEmpty(result.getDestinyMemberships()))
        .flatMap(result -> defaultBungieClient.getGroupsForMember(3,
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
    var asyncScheduler = Schedulers.boundedElastic();
    var raidsAsync = processRaidsResponseUser(interaction)
        .doOnSuccess(i -> log.info("Started processing raid stats for user. [{}]", i))
        .flatMap(response -> discordClient.editOriginalInteraction(
            interaction.getToken(), response))
        .subscribeOn(asyncScheduler);

    raidsAsync.subscribe();

    return Mono.just(InteractionResponse.builder()
        .type(5)
        .data(new InteractionResponseData())
        .build());
  }

  private Mono<MultiValueMap<String, String>> processRaidsResponseUser(Interaction interaction) {
    return raidStatsService.retrieveRaidStatsForUser(interaction)
        .map(response -> response.entrySet().stream()
            .map(entry -> EmbeddedField.builder()
                .name(entry.getKey())
                .value(entry.getValue().toString())
                .inline(true)
                .build())
            .toList())
        .map(embeddedFields -> {
          MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
          try {
            params.add("embeds",
                objectMapper.writeValueAsString(Embedded.builder().fields(embeddedFields).build()));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          return params;
        });
  }
}
