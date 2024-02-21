package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.Activity;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.entity.UserRaidDetails;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import com.danielvm.destiny2bot.enums.RaidDifficulty;
import com.danielvm.destiny2bot.exception.BadRequestException;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import java.time.Instant;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidStatsService {

  private static final Integer MAX_PAGE_COUNT = 250;
  private static final Integer RAID_MODE = 4;
  private static final String USERNAME_OPTION = "username";
  private static final String EMPTY_RAID_NAME = "empty_name";

  private final BungieClient defaultBungieClient;
  private final BungieClientWrapper bungieClientWrapper;
  private final UserDetailsRepository userDetailsRepository;
  private final PostGameCarnageService postGameCarnageService;

  public RaidStatsService(
      BungieClient defaultBungieClient,
      BungieClientWrapper bungieClientWrapper,
      UserDetailsRepository userDetailsRepository,
      PostGameCarnageService postGameCarnageService) {
    this.defaultBungieClient = defaultBungieClient;
    this.bungieClientWrapper = bungieClientWrapper;
    this.userDetailsRepository = userDetailsRepository;
    this.postGameCarnageService = postGameCarnageService;
  }

  private static RaidStatistics reduceStatistics(RaidStatistics stats,
      UserRaidDetails details) {
    stats.setTotalKills(stats.getTotalKills() + details.getTotalKills());
    stats.setTotalDeaths(stats.getTotalDeaths() + details.getTotalDeaths());
    if (details.getIsCompleted()) {
      stats.setTotalClears(stats.getTotalClears() + 1);
      if (details.getFromBeginning()) {
        stats.setFastestTime(Math.min(stats.getFastestTime(), details.getDurationSeconds()));
        stats.setFullClears(stats.getFullClears() + 1);
      }
    }
    return stats;
  }

  /**
   * Retrieve user raid statistics based on the given interaction data
   *
   * @param interaction The Discord command interaction
   * @return Map of Raid Statistics, the key will be the raid you want stats for
   */
  public Mono<Map<String, RaidStatistics>> calculateRaidLevelStats(Interaction interaction) {
    Instant now = Instant.now();
    var parsedData = (UserChoiceValue) interaction.getData().getOptions().stream()
        .filter(option -> option.getName().equalsIgnoreCase(USERNAME_OPTION))
        .findFirst()
        .orElseThrow(
            () -> new BadRequestException("No username option present", HttpStatus.BAD_REQUEST))
        .getValue();

    String membershipId = parsedData.getMembershipId();
    Integer membershipType = parsedData.getMembershipType();
    String userId = parsedData.getBungieDisplayName() + "#" + parsedData.getBungieDisplayCode();

    Mono<UserDetails> updateAction = createUserDetails(
        now, parsedData, membershipId, membershipType, userId);

    Mono<UserDetails> createAction = updateUserDetails(
        now, membershipId, membershipType, userId);

    return userDetailsRepository.existsById(userId)
        .flatMap(exists -> exists ? createAction : updateAction)
        .flatMapIterable(UserDetails::getUserRaidDetails)
        .groupBy(UserRaidDetails::getRaidName)
        .flatMap(group -> group.reduce(new RaidStatistics(group.key()),
            RaidStatsService::reduceStatistics))
        .collectMap(RaidStatistics::getRaidName, raidStatistics -> {
          if (raidStatistics.getFastestTime() == Integer.MAX_VALUE) {
            raidStatistics.setFastestTime(0);
          }
          return raidStatistics;
        });
  }

  private Mono<UserDetails> createUserDetails(Instant now, UserChoiceValue parsedData,
      String membershipId, Integer membershipType, String userId) {
    return defaultBungieClient.getUserCharacters(membershipType,
            membershipId)
        .flatMapMany(userCharacter -> Flux.fromIterable(
            userCharacter.getResponse().getCharacters().getData().keySet()))
        .flatMap(characterId -> getActivities(membershipType, membershipId, characterId))
        .flatMap(this::buildRaidDetails)
        .flatMap(this::addPGCRDetails)
        .collectList()
        .flatMap(raidDetails -> userDetailsRepository.save(
            new UserDetails(userId, parsedData.getClanName(), now, raidDetails)));
  }

  private Mono<UserDetails> updateUserDetails(Instant now, String membershipId,
      Integer membershipType, String userId) {
    return userDetailsRepository.findById(userId)
        .flatMap(userDetails -> Mono.just(userDetails.getLastRequestDateTime())
            .flatMap(
                lastCall -> defaultBungieClient.getUserCharacters(membershipType, membershipId))
            .flatMapIterable(response -> response.getResponse().getCharacters().getData().keySet())
            .flatMap(
                characterId -> getActivitiesUntil(membershipType, membershipId, characterId, now))
            .flatMap(this::buildRaidDetails)
            .flatMap(this::addPGCRDetails)
            .collectList()
            .flatMap(raidDetails -> {
              userDetails.getUserRaidDetails().addAll(raidDetails);
              userDetails.setLastRequestDateTime(now);
              return userDetailsRepository.save(userDetails);
            }));
  }

  /**
   * Returns the aggregated activities for a given Destiny 2 character. It keeps requesting new
   * pages from the API until the amount of activities is less than the max count available to
   * request
   *
   * @param membershipType The user's membershipType
   * @param membershipId   The user's membershipId
   * @param characterId    The user's characterId
   * @return a Flux containing all the activities for a character
   */
  public Flux<Activity> getActivities(Integer membershipType, String membershipId,
      String characterId) {
    return Flux.range(0, 25)
        .flatMapSequential(
            page -> defaultBungieClient.getActivityHistory(membershipType, membershipId,
                characterId, MAX_PAGE_COUNT, RAID_MODE, page))
        .filter(activities -> CollectionUtils.isNotEmpty(activities.getResponse().getActivities()))
        .takeUntil(activities -> activities.getResponse().getActivities().size() < MAX_PAGE_COUNT)
        .flatMapIterable(response -> response.getResponse().getActivities());
  }

  /**
   * Retrieves all the activities that the Bot is not aware of for a user since the last time the
   * user was requested. Like the getActivities() method, it keeps requesting new pages from the API
   * until the amount of elements in the page is less than the MAX_COUNT which is 250 elements
   *
   * @param membershipType The membershipType of the user
   * @param membershipId   The membershipId of the user
   * @param characterId    The ID of the current character
   * @param until          The instant until we are retrieving elements
   * @return a Flux containing all missing activities up until now
   */
  public Flux<Activity> getActivitiesUntil(Integer membershipType, String membershipId,
      String characterId, Instant until) {
    Predicate<Activity> newRaidPredicate = activity -> activity.getPeriod().isBefore(until);

    return Flux.range(0, 25)
        .flatMapSequential(
            page -> defaultBungieClient.getActivityHistory(membershipType, membershipId,
                characterId, MAX_PAGE_COUNT, RAID_MODE, page))
        .filter(response -> CollectionUtils.isNotEmpty(response.getResponse().getActivities()))
        .takeUntil(response -> response.getResponse().getActivities().stream()
            .anyMatch(newRaidPredicate))
        .flatMapIterable(response -> response.getResponse().getActivities())
        .filter(newRaidPredicate);
  }

  private Mono<UserRaidDetails> buildRaidDetails(Activity activity) {
    return bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION,
            activity.getActivityDetails().getDirectorActivityHash())
        .map(entity -> {
              boolean emptyRaidDetails = entity.getResponse().getDisplayProperties() == null ||
                                         entity.getResponse().getDisplayProperties().getName() == null;
              String raidName = emptyRaidDetails ? EMPTY_RAID_NAME :
                  resolveRaidName(entity.getResponse().getDisplayProperties().getName());
              RaidDifficulty raidDifficulty = emptyRaidDetails ? null :
                  resolveRaidDifficult(entity.getResponse().getDisplayProperties().getName());

              return UserRaidDetails.builder()
                  .raidName(raidName)
                  .raidDifficulty(raidDifficulty)
                  .totalDeaths(activity.getValues().get("deaths").getBasic().getValue().intValue())
                  .totalKills(activity.getValues().get("kills").getBasic().getValue().intValue())
                  .kda(activity.getValues().get("killsDeathsAssists").getBasic().getValue())
                  .durationSeconds(
                      activity.getValues().get("activityDurationSeconds").getBasic().getValue()
                          .intValue())
                  .isCompleted(activity.getValues().get("completed").getBasic().getValue() != 0)
                  .build();
            }
        );
  }

  private Mono<UserRaidDetails> addPGCRDetails(UserRaidDetails userRaidDetails) {
    return postGameCarnageService.retrievePGCR(userRaidDetails.getInstanceId())
        .map(report -> {
          userRaidDetails.setFromBeginning(report.getFromBeginning());
          return userRaidDetails;
        });
  }

  private RaidDifficulty resolveRaidDifficult(String raidName) {
    String[] tokens = raidName.split(":");
    if (tokens.length > 1) {
      return switch (tokens[1].trim()) {
        case "Normal", "normal" -> RaidDifficulty.NORMAL;
        case "Master", "master" -> RaidDifficulty.MASTER;
      };
    }
    return null;
  }

  private String resolveRaidName(String name) {
    String[] tokens = name.split(":");
    return tokens[0].trim();
  }
}
