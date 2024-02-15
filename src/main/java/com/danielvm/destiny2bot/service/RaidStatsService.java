package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.dto.RaidEntry;
import com.danielvm.destiny2bot.dto.destiny.Activity;
import com.danielvm.destiny2bot.dto.destiny.CharacterRaidStatistics;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
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

  private static CharacterRaidStatistics createRaidStatistics(CharacterRaidStatistics stats,
      RaidEntry raidEntry) {
    stats.setTotalKills(stats.getTotalKills() + raidEntry.getTotalKills());
    stats.setTotalDeaths(stats.getTotalDeaths() + raidEntry.getTotalDeaths());
    stats.setTotalRuns(stats.getTotalRuns() + 1);
    if (raidEntry.getIsCompleted()) {
      stats.setClears(stats.getClears() + 1);
      if (raidEntry.getIsFromBeginning()) {
        stats.setFastestTime(Math.min(stats.getFastestTime(), raidEntry.getDuration()));
        stats.setFullClears(stats.getFullClears() + 1);
      }
    } else {
      stats.setUncompleted(stats.getUncompleted() + 1);
    }
    return stats;
  }

  public Mono<Map<String, CharacterRaidStatistics>> retrieveRaidStatsForUser(
      Interaction interaction) {
    String membershipId = (String) interaction.getData().getOptions().get(0).getValue();
    return defaultBungieClient.getUserCharacters(3, membershipId)
        .flatMapMany(userCharacter -> Flux.fromIterable(
            userCharacter.getResponse().getCharacters().getData().keySet()))
        .flatMap(characterId -> getActivities(membershipId, characterId))
        .flatMap(this::createRaidEntry)
        .flatMap(this::addPGCRDetails, 25)
        .groupBy(RaidEntry::getRaidName)
        .flatMap(group -> group.reduce(new CharacterRaidStatistics(group.key()),
            RaidStatsService::createRaidStatistics))
        .collectMap(CharacterRaidStatistics::getRaidName, characterRaidStatistics -> {
          if (characterRaidStatistics.getFastestTime() == Integer.MAX_VALUE) {
            characterRaidStatistics.setFastestTime(0);
          }
          return characterRaidStatistics;
        });
  }

  private Mono<RaidEntry> addPGCRDetails(RaidEntry raidEntry) {
    return pgcrBungieClient.getPostGameCarnageReport(raidEntry.getInstanceId())
        .map(pgcr -> {
          raidEntry.setIsFromBeginning(pgcr.getResponse().getActivityWasStartedFromBeginning());
          return raidEntry;
        });
  }

  private Flux<Activity> getActivities(String membershipId, String characterId) {
    return Flux.range(0, 25)
        .flatMapSequential(
            page -> defaultBungieClient.getActivityHistory(3, membershipId, characterId,
                MAX_PAGE_COUNT, RAID_MODE, page))
        .filter(activities -> CollectionUtils.isNotEmpty(activities.getResponse().getActivities()))
        .takeUntil(activities -> activities.getResponse().getActivities().size() < 250)
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
