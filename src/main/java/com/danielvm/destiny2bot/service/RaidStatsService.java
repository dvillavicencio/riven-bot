package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.entity.UserRaidDetails;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidStatsService {

  private final UserRaidDetailsService userRaidDetailsService;

  public RaidStatsService(
      UserRaidDetailsService userRaidDetailsService) {
    this.userRaidDetailsService = userRaidDetailsService;
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
   * Calculate user raid statistics based on the parsed data from a Discord option value. This
   * method returns a map of the raid stats grouped by the raid name.
   *
   * @param parsedData The parsed data needed to retrieve Raid Statistics for a player
   * @return Map of Raid Statistics grouped by raid name
   */
  public Mono<Map<String, RaidStatistics>> calculateRaidStats(UserChoiceValue parsedData) {
    Instant now = Instant.now(); // Timestamp for this action
    String userId = parsedData.getBungieDisplayName() + "#" + parsedData.getBungieDisplayCode();

    Mono<UserDetails> createAction = userRaidDetailsService.createUserDetails(now, parsedData)
        .doOnSubscribe(subscription -> log.info("Creation action initiated for user [{}]", userId))
        .doOnSuccess(userDetails -> log.info("Creation action finished for user [{}]", userId));

    Mono<UserDetails> updateAction = userRaidDetailsService.updateUserDetails(now, parsedData)
        .doOnSubscribe(subscription -> log.info("Update action initiated for user [{}]", userId))
        .doOnSuccess(userDetails -> log.info("Update action finished for user [{}]", userId));

    return userRaidDetailsService.existsById(userId)
        .flatMap(exists -> exists ? updateAction : createAction)
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
}
