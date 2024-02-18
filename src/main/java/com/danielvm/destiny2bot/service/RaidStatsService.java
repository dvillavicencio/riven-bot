package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.dto.RaidEntry;
import com.danielvm.destiny2bot.dto.destiny.Activity;
import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.PostGameCarnageReport;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidStatsService {

  private static final Integer MAX_PAGE_COUNT = 250;
  private static final Integer RAID_MODE = 4;

  private final BungieClient defaultBungieClient;
  private final BungieClient pgcrBungieClient;
  private final BungieClientWrapper bungieClientWrapper;

  public RaidStatsService(
      BungieClient defaultBungieClient,
      BungieClient pgcrBungieClient,
      BungieClientWrapper bungieClientWrapper) {
    this.defaultBungieClient = defaultBungieClient;
    this.pgcrBungieClient = pgcrBungieClient;
    this.bungieClientWrapper = bungieClientWrapper;
  }

  private static RaidStatistics createRaidStatistics(RaidStatistics stats, RaidEntry raidEntry) {
    stats.setTotalKills(stats.getTotalKills() + raidEntry.getTotalKills());
    stats.setTotalDeaths(stats.getTotalDeaths() + raidEntry.getTotalDeaths());
    if (raidEntry.getIsCompleted()) {
      stats.setTotalClears(stats.getTotalClears() + 1);
      if (raidEntry.getIsFromBeginning()) {
        stats.setFastestTime(Math.min(stats.getFastestTime(), raidEntry.getDuration()));
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
  public Mono<Map<String, RaidStatistics>> calculateRaidLevelStats(
      Interaction interaction) {
    var parsedData = ((String) interaction.getData().getOptions().get(0).getValue()).split(":");
    String membershipId = parsedData[0];
    Integer membershipType = Integer.valueOf(parsedData[1]);

    return defaultBungieClient.getUserCharacters(membershipType, membershipId)
        .flatMapMany(userCharacter -> Flux.fromIterable(
            userCharacter.getResponse().getCharacters().getData().keySet()))
        .flatMap(characterId -> getActivities(membershipType, membershipId, characterId))
        .flatMap(this::createRaidEntry)
        .flatMap(this::addPGCRDetails, 5)
        .groupBy(RaidEntry::getRaidName)
        .flatMap(group -> group.reduce(new RaidStatistics(group.key()),
            RaidStatsService::createRaidStatistics))
        .collectMap(RaidStatistics::getRaidName, raidStatistics -> {
          if (raidStatistics.getFastestTime() == Integer.MAX_VALUE) {
            raidStatistics.setFastestTime(0);
          }
          return raidStatistics;
        })
        .doOnSubscribe(c -> log.info("Calculating raid statistics for user [{}]", parsedData[2]))
        .doOnSuccess(
            c -> log.info("Finished calculating raid statistics for user [{}]", parsedData[2]));
  }

  private Mono<RaidEntry> addPGCRDetails(RaidEntry raidEntry) {
    return pgcrBungieClient.getPostGameCarnageReport(raidEntry.getInstanceId())
        .onErrorResume(WebClientException.class, err -> {
          log.warn("Response too big to parse, ignoring and falling back to default value");
          return Mono.just(new BungieResponse<>(
              new PostGameCarnageReport(null, false, Collections.emptyList())));
        })
        .map(pgcr -> {
          raidEntry.setIsFromBeginning(pgcr.getResponse().getActivityWasStartedFromBeginning());
          return raidEntry;
        });
  }

  private Flux<Activity> getActivities(Integer membershipType, String membershipId,
      String characterId) {
    return Flux.range(0, 25)
        .flatMapSequential(
            page -> defaultBungieClient.getActivityHistory(membershipType, membershipId,
                characterId, MAX_PAGE_COUNT, RAID_MODE, page))
        .filter(activities -> CollectionUtils.isNotEmpty(activities.getResponse().getActivities()))
        .takeUntil(activities -> activities.getResponse().getActivities().size() < MAX_PAGE_COUNT)
        .flatMapIterable(response -> response.getResponse().getActivities());
  }

  private Mono<RaidEntry> createRaidEntry(Activity activity) {
    return bungieClientWrapper.getManifestEntityRx(ManifestEntity.ACTIVITY_DEFINITION,
            String.valueOf(activity.getActivityDetails().getDirectorActivityHash()))
        .map(entity -> {
          if (entity.getResponse() == null || entity.getResponse().getDisplayProperties() == null
              || entity.getResponse().getDisplayProperties().getName() == null) {
            return "";
          }
          return resolveRaidName(entity.getResponse().getDisplayProperties().getName());
        })
        .map(raidName -> new RaidEntry(raidName,
            activity.getActivityDetails().getInstanceId(),
            activity.getValues().get("deaths").getBasic().getValue().intValue(),
            activity.getValues().get("kills").getBasic().getValue().intValue(),
            activity.getValues().get("killsDeathsAssists").getBasic().getValue(),
            activity.getValues().get("activityDurationSeconds").getBasic().getValue().intValue(),
            activity.getValues().get("completed").getBasic().getValue() != 0,
            null
        ));
  }

  private String resolveRaidName(String name) {
    String[] tokens = name.split(":");
    return tokens[0].trim();
  }
}
