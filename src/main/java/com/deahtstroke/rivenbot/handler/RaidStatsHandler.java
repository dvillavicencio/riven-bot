package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.config.BungieConfiguration;
import com.deahtstroke.rivenbot.dto.destiny.MemberGroupResponse;
import com.deahtstroke.rivenbot.dto.destiny.UserGlobalSearchBody;
import com.deahtstroke.rivenbot.dto.destiny.UserSearchResult;
import com.deahtstroke.rivenbot.dto.discord.Choice;
import com.deahtstroke.rivenbot.dto.discord.Component;
import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedAuthor;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedFooter;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.entity.RaidStatistics;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.service.BungieAPIService;
import com.deahtstroke.rivenbot.service.DiscordAPIService;
import com.deahtstroke.rivenbot.service.RaidStatsService;
import com.deahtstroke.rivenbot.util.NumberUtils;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@org.springframework.stereotype.Component
public class RaidStatsHandler implements AutocompleteSource, ApplicationCommandSource {

  private static final String STATS_TITLE = "Raid Stats for %s";
  private static final String ICON_BASE_URL = "https://www.bungie.net/";
  private static final String USERNAME_OPTION_NAME = "username";
  private static final Integer CLAN_GROUP_TYPE = 1;
  private static final Integer CLAN_SIZE_FILTER = 0;
  private static final Integer MAX_NUMBER_OF_USER_PAGES = 15;
  private static final Integer NO_USERS_FOUND_ERROR_CODE = 217;
  private static final String HASHTAG = "#";
  private static final String LEFT_BRACKET = "[";
  private static final String RIGHT_BRACKET = "]";
  private static final String RAID_COMPREHENSION_BUTTON_ID = "raid_stats_comprehension";
  private static final String DEVELOPMENT_NOTICE = """
      Keep in mind this command is still being developed and the data displayed may be inaccurate. \
                                  
      For example, fastest clears for Last Wish *could* be incorrect because of the Wall of Wishes, \
      meaning that if the raid was started from the beginning, but you used a wish to get to \
      Riven and finish the raid in under ~10 minutes, the bot would still count that as your fastest clear.""";

  private final BungieClient defaultBungieClient;
  private final RaidStatsService raidStatsService;
  private final DiscordAPIService discordAPIService;
  private final BungieAPIService bungieAPIService;

  public RaidStatsHandler(
      BungieClient defaultBungieClient,
      RaidStatsService raidStatsService,
      DiscordAPIService discordAPIService,
      BungieAPIService bungieAPIService) {
    this.defaultBungieClient = defaultBungieClient;
    this.raidStatsService = raidStatsService;
    this.discordAPIService = discordAPIService;
    this.bungieAPIService = bungieAPIService;
  }

  @Override
  public Mono<InteractionResponse> autocomplete(Interaction interaction) {
    Option usernameOption = interaction.getData().getOptions().stream()
        .filter(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION_NAME))
        .findAny().orElse(new Option());
    String usernameInput = (String) usernameOption.getValue();
    String[] tokens = usernameInput.split(HASHTAG);
    Integer tag;
    String username;
    if (tokens.length > 1) {
      if (NumberUtils.isInteger(tokens[0])) {
        tag = Integer.parseInt(tokens[0]);
        username = tokens[1];
      } else {
        tag = Integer.parseInt(tokens[1]);
        username = tokens[0];
      }
    } else if (tokens.length == 1) {
      username = tokens[0];
      tag = null;
    } else {
      username = "";
      tag = 0;
    }
    return Flux.range(0, MAX_NUMBER_OF_USER_PAGES)
        .flatMapSequential(pageNumber -> bungieAPIService.retrievePlayers(
            new UserGlobalSearchBody(username), pageNumber))
        .transformDeferred(RateLimiterOperator.of(BungieConfiguration.PGCR_RATE_LIMITER))
        .takeWhile(response -> !Objects.equals(response.getErrorCode(), NO_USERS_FOUND_ERROR_CODE))
        .flatMapIterable(response -> response.getResponse().getSearchResults())
        .filter(result -> CollectionUtils.isNotEmpty(result.getDestinyMemberships()))
        .filter(result -> tag == null || NumberUtils.contains(
            result.getBungieGlobalDisplayNameCode(), tag))
        .take(25)
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
    String membershipId = result.getDestinyMemberships().get(0).getMembershipId();
    Integer membershipType = result.getDestinyMemberships().get(0).getMembershipType();
    return defaultBungieClient.getGroupsForMember(membershipType, membershipId,
            CLAN_SIZE_FILTER, CLAN_GROUP_TYPE)
        .map(clanResponse -> {
          String choiceName = name(result, clanResponse.getResponse());
          String choiceValue = value(result);
          return new Choice(choiceName, choiceValue);
        });
  }

  @Override
  public Mono<InteractionResponse> resolve(Interaction interaction) {
    var raidsAsyncProcessing = processRaidsAsync(interaction)
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(response ->
            discordAPIService.editOriginalInteraction(interaction.getToken(), response));
    raidsAsyncProcessing.subscribe();

    InteractionResponse ack = InteractionResponse.builder()
        .type(5)
        .data(new InteractionResponseData())
        .build();

    return Mono.just(ack);
  }

  private Mono<InteractionResponseData> processRaidsAsync(Interaction interaction) {
    return Mono.just(interaction).flatMap(i -> {
      Object optionValue = i.getData().getOptions().get(0).getOptions().get(0).getValue();
      String[] values = ((String) optionValue).split(":");
      Integer membershipType = Integer.valueOf(values[0]);
      String membershipId = values[1];
      return createRaidStatsResponse(membershipType, membershipId);
    });
  }

  private Mono<InteractionResponseData> createRaidStatsResponse(Integer membershipType,
      String membershipId) {
    return defaultBungieClient.getMembershipInfoById(membershipId, membershipType)
        .flatMap(bungieUser -> {
          String uniqueUsername = bungieUser.getResponse().getBungieNetUser().getUniqueName();
          String usernameIcon = ICON_BASE_URL + bungieUser.getResponse().getBungieNetUser()
              .getProfilePicturePath();
          return raidStatsService.calculateRaidStats(uniqueUsername, membershipId, membershipType)
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
                              .name(uniqueUsername)
                              .iconUrl(usernameIcon)
                              .build())
                          .title(STATS_TITLE.formatted(uniqueUsername))
                          .description("""
                              <t:%s:R>
                              General crunched numbers regarding all the raid clears you've done so far guardian.
                              """.formatted(Instant.now().getEpochSecond()))
                          .fields(embeddedFields)
                          .color(10070709)
                          .footer(EmbeddedFooter.builder()
                              .text(DEVELOPMENT_NOTICE)
                              .build())
                          .build()))
                  .components(List.of(Component.builder()
                      .type(1)
                      .components(List.of(Component.builder()
                          .type(2)
                          .customId(RAID_COMPREHENSION_BUTTON_ID)
                          .style(1)
                          .label("What is this?")
                          .build()))
                      .build()))
                  .build());
        });
  }

  private String name(UserSearchResult result, MemberGroupResponse groupResponse) {
    StringBuilder defaultName = new StringBuilder()
        .append(result.getBungieGlobalDisplayName())
        .append(HASHTAG)
        .append(result.getBungieGlobalDisplayNameCode());
    if (CollectionUtils.isNotEmpty(groupResponse.getResults())) {
      String clanName = groupResponse.getResults()
          .get(0).getGroup().getName();
      defaultName.append(LEFT_BRACKET);
      defaultName.append(clanName);
      defaultName.append(RIGHT_BRACKET);
    }
    return defaultName.toString();
  }

  private String value(UserSearchResult result) {
    return result.getBungieGlobalDisplayName()
           + HASHTAG
           + result.getBungieGlobalDisplayNameCode();
  }
}
