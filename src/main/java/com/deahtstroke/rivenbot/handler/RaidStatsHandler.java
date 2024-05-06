package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.client.DiscordClient;
import com.deahtstroke.rivenbot.config.DiscordConfiguration;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchRequest;
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
import com.deahtstroke.rivenbot.exception.BadRequestException;
import com.deahtstroke.rivenbot.service.RaidStatsService;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@org.springframework.stereotype.Component
public class RaidStatsHandler implements AutocompleteSource, ApplicationCommandSource {

  private static final String STATS_TITLE = "Raid Stats for %s";
  private static final String ICON_BASE_URL = "https://www.bungie.net/";
  private static final String CHOICE_FORMAT = "%s:%s";
  private static final String USER_TAG_OPTION_NAME = "usertag";
  private static final String USERNAME_OPTION_NAME = "username";
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
    Option usernameLookupOption = interaction.getData().getOptions().stream()
        .filter(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION_NAME)).findFirst()
        .orElseThrow(
            () -> new BadRequestException("Username option not found", HttpStatus.BAD_REQUEST))
        .getOptions().getFirst();
    return defaultBungieClient.searchByGlobalName(
            new UserGlobalSearchBody((String) usernameLookupOption.getValue()), 0)
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
    String membershipId = result.getDestinyMemberships().get(0).getMembershipId();
    Integer membershipType = result.getDestinyMemberships().get(0).getMembershipType();
    return defaultBungieClient.getGroupsForMember(membershipType, membershipId, CLAN_SIZE_FILTER,
            CLAN_GROUP_TYPE)
        .map(clanResponse -> {
          String choiceName = name(result, clanResponse.getResponse());
          String choiceValue = value(membershipId, membershipType);
          return new Choice(choiceName, choiceValue);
        });
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    var asyncScheduler = Schedulers.boundedElastic();
    var raidsAsync = processRaidsAsynchronously(interaction)
        .subscribeOn(asyncScheduler)
        .flatMap(response -> discordClient.editOriginalInteraction(
            discordConfiguration.getApplicationId(), interaction.getToken(), response));

    raidsAsync.subscribe();

    return Mono.just(InteractionResponse.builder()
        .type(5)
        .data(new InteractionResponseData())
        .build());
  }

  private Mono<InteractionResponseData> processRaidsAsynchronously(Interaction interaction) {
    return Mono.just(interaction).flatMap(i -> {
      var usernameOnly = interaction.getData().getOptions().stream()
          .anyMatch(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION_NAME));
      // If the request only has username
      if (usernameOnly) {
        Object optionValue = i.getData().getOptions().get(0).getOptions().get(0).getValue();

        String[] values = ((String) optionValue).split(":");
        Integer membershipType = Integer.valueOf(values[0]);
        String membershipId = values[1];
        return createRaidStatsResponse(membershipType, membershipId);
      } else {
        String username = (String) i.getData().getOptions().get(0).getOptions().stream()
            .filter(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION_NAME))
            .findFirst().orElseThrow().getValue();
        Integer userTag = (Integer) i.getData().getOptions().get(0).getOptions().stream()
            .filter(option -> option.getName().equalsIgnoreCase(USER_TAG_OPTION_NAME))
            .findFirst().orElseThrow().getValue();
        ExactUserSearchRequest request = ExactUserSearchRequest.builder()
            .displayName(username)
            .displayNameCode(userTag)
            .build();
        return defaultBungieClient.searchUserByExactNameAndCode(request)
            .flatMap(response -> {
              Integer membershipType = response.getResponse().get(0).getMembershipType();
              String membershipId = response.getResponse().get(0).getMembershipId();
              return createRaidStatsResponse(membershipType, membershipId);
            });
      }
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
                              .text("""
                                  Keep in mind this command is still being developed and the data displayed may be inaccurate. \
                                                              
                                  For example, fastest clears for Last Wish *could* be incorrect because of the Wall of Wishes, \
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
        });
  }

  private String name(UserSearchResult result,
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

  private String value(String membershipId, Integer membershipType) {
    return CHOICE_FORMAT.formatted(membershipType, membershipId);
  }
}
