package com.danielvm.destiny2bot.handler;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.DiscordClient;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.MemberGroupResponse;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.dto.destiny.UserGlobalSearchBody;
import com.danielvm.destiny2bot.dto.destiny.UserSearchResult;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Component;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.EmbeddedAuthor;
import com.danielvm.destiny2bot.dto.discord.EmbeddedField;
import com.danielvm.destiny2bot.dto.discord.EmbeddedFooter;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import com.danielvm.destiny2bot.service.RaidStatsService;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@org.springframework.stereotype.Component
public class RaidStatsHandler implements AutocompleteSource, ApplicationCommandSource {

  private static final String STATS_TITLE = "Raid Stats for %s";
  private static final Integer CLAN_GROUP_TYPE = 1;
  private static final Integer CLAN_SIZE_FILTER = 0;

  private final BungieClient defaultBungieClient;
  private final DiscordClient discordClient;
  private final RaidStatsService raidStatsService;
  private final DiscordConfiguration discordConfiguration;

  public RaidStatsHandler(
      BungieClient defaultBungieClient,
      DiscordClient discordClient,
      RaidStatsService raidStatsService,
      DiscordConfiguration discordConfiguration) {
    this.defaultBungieClient = defaultBungieClient;
    this.discordClient = discordClient;
    this.raidStatsService = raidStatsService;
    this.discordConfiguration = discordConfiguration;
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    Object focusedOption = interaction.getData().getOptions().get(0).getValue();
    return defaultBungieClient.searchByGlobalName(new UserGlobalSearchBody((String) focusedOption),
            0)
        .flatMapIterable(response -> response.getResponse().getSearchResults())
        .take(25)
        .filter(result -> CollectionUtils.isNotEmpty(result.getDestinyMemberships()))
        .flatMap(this::createUserChoices)
        .collectList()
        .map(choices -> new InteractionResponse(
            InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType(),
            InteractionResponseData.builder()
                .choices(choices)
                .build()
        ));
  }

  private Mono<Choice> createUserChoices(UserSearchResult result) {
    String membershipId = result.getDestinyMemberships().get(0).membershipId();
    Integer membershipType = result.getDestinyMemberships().get(0).membershipType();
    return defaultBungieClient.getGroupsForMember(membershipType, membershipId, CLAN_SIZE_FILTER,
            CLAN_GROUP_TYPE)
        .map(clanResponse -> {
          String defaultName = choiceName(result, clanResponse.getResponse());
          UserChoiceValue choiceValue = choiceValue(result, clanResponse.getResponse());
          return new Choice(defaultName, choiceValue);
        });
  }

  private UserChoiceValue choiceValue(UserSearchResult result,
      MemberGroupResponse clanResponse) {
    UserChoiceValue value = new UserChoiceValue();
    value.setMembershipId(result.getDestinyMemberships().get(0).membershipId());
    value.setMembershipType(result.getDestinyMemberships().get(0).membershipType());
    value.setBungieDisplayName(result.getBungieGlobalDisplayName());
    value.setBungieDisplayCode(result.getBungieGlobalDisplayNameCode());
    if (clanResponse != null && CollectionUtils.isNotEmpty(clanResponse.getResults())) {
      value.setClanName(clanResponse.getResults().get(0).getGroup().getName());
    }
    return value;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    var asyncScheduler = Schedulers.boundedElastic();
    var raidsAsync = processRaidsResponseUser(interaction)
        .flatMap(response -> discordClient.editOriginalInteraction(
            discordConfiguration.getApplicationId(), interaction.getToken(), response))
        .subscribeOn(asyncScheduler);

    raidsAsync.subscribe();

    return Mono.just(InteractionResponse.builder()
        .type(5)
        .data(new InteractionResponseData())
        .build());
  }

  private Mono<InteractionResponseData> processRaidsResponseUser(Interaction interaction) {
    UserChoiceValue parsedData = ((UserChoiceValue) interaction.getData().getOptions().get(0)
        .getValue());
    return raidStatsService.calculateRaidStats(parsedData)
        .collectMap(RaidStatistics::get_id)
        .map(response -> response.entrySet().stream()
            .map(entry -> EmbeddedField.builder()
                .name(entry.getKey())
                .value(entry.getValue().toString())
                .inline(true)
                .build())
            .toList())
        .map(embeddedFields -> InteractionResponseData.builder()
            .embeds(List.of(
                Embedded.builder()
                    .author(EmbeddedAuthor.builder()
                        .name("Riven of a Thousand Servers")
                        .iconUrl(
                            "https://ih1.redbubble.net/image.2953200665.7291/st,small,507x507-pad,600x600,f8f8f8.jpg")
                        .build())
                    .title(STATS_TITLE.formatted(parsedData.getBungieDisplayCode()))
                    .description("""
                        <t:%s:R>
                        General crunched numbers regarding all the raid clears you've done so far guardian.
                        """.formatted(Instant.now().getEpochSecond()))
                    .fields(embeddedFields)
                    .color(10070709)
                    .footer(EmbeddedFooter.builder()
                        .text("""
                            Keep in mind this command is still being developed and the data displayed may be inaccurate. \
                                                        
                            For example, fastest clears for Last Wish could be incorrect because of the Wall of Wishes, \
                            meaning that if the raid was started from the beginning, but you used a wish to get to \
                            Riven and finish the raid in under ~10 minutes, the bot would still count that as your fastest clear.""")
                        .build())
                    .build()))
            .components(List.of(Component.builder()
                .type(1)
                .components(List.of(Component.builder()
                    .type(2)
                    .customId("raid_stats_comprehension")
                    .style(1)
                    .label("What is this?")
                    .build()))
                .build()))
            .build());
  }

  private String choiceName(UserSearchResult result,
      MemberGroupResponse groupResponse) {
    StringBuilder defaultName = new StringBuilder()
        .append(result.getBungieGlobalDisplayName())
        .append("#")
        .append(result.getBungieGlobalDisplayNameCode());
    if (CollectionUtils.isNotEmpty(groupResponse.getResults())) {
      String clanName = groupResponse.getResults()
          .get(0).getGroup().getName();
      defaultName.append("[");
      defaultName.append(clanName);
      defaultName.append("]");
    }
    return defaultName.toString();
  }
}
