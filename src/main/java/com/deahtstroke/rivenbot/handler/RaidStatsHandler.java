package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.config.BungieConfiguration;
import com.deahtstroke.rivenbot.dto.destiny.MemberGroupResponse;
import com.deahtstroke.rivenbot.dto.destiny.UserGlobalSearchBody;
import com.deahtstroke.rivenbot.dto.destiny.UserSearchResult;
import com.deahtstroke.rivenbot.dto.discord.Choice;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.processor.AsyncRaidsProcessor;
import com.deahtstroke.rivenbot.service.BungieAPIService;
import com.deahtstroke.rivenbot.util.NumberUtils;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class RaidStatsHandler implements AutocompleteSource, ApplicationCommandSource {

  private static final String USERNAME_OPTION_NAME = "username";
  private static final Integer CLAN_GROUP_TYPE = 1;
  private static final Integer CLAN_SIZE_FILTER = 0;
  private static final Integer MAX_NUMBER_OF_USER_PAGES = 15;
  private static final Integer NO_USERS_FOUND_ERROR_CODE = 217;
  private static final String HASHTAG = "#";
  private static final String EMPTY_STRING = "";
  private static final String LEFT_BRACKET = "[";
  private static final String RIGHT_BRACKET = "]";

  private final BungieClient defaultBungieClient;
  private final BungieAPIService bungieAPIService;
  private final AsyncRaidsProcessor asyncRaidsProcessor;

  public RaidStatsHandler(
      BungieClient defaultBungieClient,
      BungieAPIService bungieAPIService,
      AsyncRaidsProcessor asyncRaidsProcessor) {
    this.defaultBungieClient = defaultBungieClient;
    this.bungieAPIService = bungieAPIService;
    this.asyncRaidsProcessor = asyncRaidsProcessor;
  }

  @Override
  public Mono<InteractionResponse> handle(Interaction interaction) {
    Option usernameOption = interaction.getData().getOptions().stream()
        .filter(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION_NAME))
        .findAny().orElse(new Option());
    String usernameInput = (String) usernameOption.getValue();
    String[] tokens = usernameInput.split(HASHTAG);
    String tag;
    String username;
    if (tokens.length > 1) {
      if (NumberUtils.isInteger(tokens[0])) {
        tag = tokens[0];
        username = tokens[1];
      } else {
        tag = tokens[1];
        username = tokens[0];
      }
    } else if (tokens.length == 1) {
      username = tokens[0];
      tag = EMPTY_STRING;
    } else {
      username = EMPTY_STRING;
      tag = EMPTY_STRING;
    }
    return Flux.range(0, MAX_NUMBER_OF_USER_PAGES)
        .flatMapSequential(pageNumber -> bungieAPIService.retrievePlayers(
            new UserGlobalSearchBody(username), pageNumber))
        .transformDeferred(RateLimiterOperator.of(BungieConfiguration.PGCR_RATE_LIMITER))
        .takeWhile(response -> !Objects.equals(response.getErrorCode(), NO_USERS_FOUND_ERROR_CODE))
        .flatMapIterable(response -> response.getResponse().getSearchResults())
        .filter(result -> CollectionUtils.isNotEmpty(result.getDestinyMemberships()))
        .filter(result -> levenshteinFilter(tag, result))
        .take(25)
        .flatMap(this::createUserChoices)
        .collectList()
        .map(choices -> new InteractionResponse(
            InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType(),
            InteractionResponseData.builder().choices(choices)
                .build()
        ));
  }

  private boolean levenshteinFilter(String tag, UserSearchResult result) {
    if (Objects.equals(tag, EMPTY_STRING)) {
      return true;
    } else {
      Integer threshold = resolveThreshold(tag.length());
      LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
      return levenshteinDistance.apply(tag,
          String.valueOf(result.getBungieGlobalDisplayNameCode())) <= threshold;
    }
  }

  private Integer resolveThreshold(Integer tagLength) {
    return switch (tagLength) {
      case 2 -> 2;
      case 3 -> 1;
      case 4 -> 0;
      default -> 3;
    };
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
    return Mono.just(InteractionResponse.builder()
            .type(5)
            .data(new InteractionResponseData())
            .build())
        .publishOn(Schedulers.boundedElastic())
        .doOnSubscribe(subscription -> {
          Object optionValue = interaction.getData().getOptions().get(0).getValue();
          String[] values = ((String) optionValue).split(HASHTAG);
          String username = values[0];
          String userTag = values[1];
          asyncRaidsProcessor.processRaidsAsync(username, userTag, interaction.getToken())
              .subscribe();
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
