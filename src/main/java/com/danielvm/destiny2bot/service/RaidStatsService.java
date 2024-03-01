package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.enums.RaidDifficulty;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidStatsService {

  private static final List<String> RAIDS_WITH_MASTER_MODE = List.of(
      "Vault of Glass", "Vow of the Disciple", "King's Fall", "Root of Nightmares", "Crota's End"
  );
  private static final String RAID_NAME = "userRaidDetails.raidName";
  private static final String IS_COMPLETED = "userRaidDetails.isCompleted";
  private static final String RAID_DIFFICULTY = "userRaidDetails.raidDifficulty";
  private static final String TOTAL_KILLS = "userRaidDetails.totalKills";
  private static final String TOTAL_DEATHS = "userRaidDetails.totalDeaths";
  private static final String FROM_BEGINNING = "userRaidDetails.fromBeginning";

  private final UserRaidDetailsService userRaidDetailsService;
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public RaidStatsService(
      UserRaidDetailsService userRaidDetailsService,
      ReactiveMongoTemplate reactiveMongoTemplate) {
    this.userRaidDetailsService = userRaidDetailsService;
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  private static Aggregation raidStatisticsAggregationPipeline(String userId) {
    MatchOperation userIdMatch = Aggregation.match(Criteria.where("_id").is(userId));

    UnwindOperation unwindRaids = new UnwindOperation(Fields.field("userRaidDetails"));

    Criteria fastestTimeCriteria = new Criteria();
    fastestTimeCriteria.andOperator(
        Criteria.where(FROM_BEGINNING).is(true),
        Criteria.where(IS_COMPLETED).is(true)
    );

    Criteria normalModeClearsCriteria = new Criteria();
    normalModeClearsCriteria.andOperator(
        Criteria.where(RAID_NAME).in(RAIDS_WITH_MASTER_MODE),
        Criteria.where(IS_COMPLETED).is(true),
        Criteria.where(RAID_DIFFICULTY).is(RaidDifficulty.NORMAL.name())
    );

    Criteria masterModeClearsCriteria = new Criteria();
    masterModeClearsCriteria.andOperator(
        Criteria.where(RAID_NAME).in(RAIDS_WITH_MASTER_MODE),
        Criteria.where(IS_COMPLETED).is(true),
        Criteria.where(RAID_DIFFICULTY).is(RaidDifficulty.MASTER.name())
    );

    Criteria raidClearCriteria = Criteria.where(IS_COMPLETED).is(true);

    Criteria fullRaidClearCriteria = new Criteria();
    fullRaidClearCriteria.andOperator(
        Criteria.where(IS_COMPLETED).is(true),
        Criteria.where(FROM_BEGINNING).is(true)
    );

    Criteria partialRaidClearCriteria = new Criteria();
    partialRaidClearCriteria.andOperator(
        Criteria.where(IS_COMPLETED).is(true),
        Criteria.where(FROM_BEGINNING).is(false)
    );
    GroupOperation groupByRaidName = Aggregation.group(RAID_NAME)
        .sum(TOTAL_KILLS).as("totalKills")
        .sum(TOTAL_DEATHS).as("totalDeaths")
        .min(ConditionalOperators
            .when(fastestTimeCriteria)
            .then("$userRaidDetails.durationSeconds")
            .otherwise("0")).as("fastestTime")
        .sum(ConditionalOperators
            .when(raidClearCriteria)
            .then(1)
            .otherwise(0)).as("totalClears")
        .sum(ConditionalOperators
            .when(partialRaidClearCriteria)
            .then(1)
            .otherwise(0)).as("partialClears")
        .sum(ConditionalOperators
            .when(fullRaidClearCriteria)
            .then(1)
            .otherwise(0)).as("fullClears")
        .sum(ConditionalOperators
            .when(normalModeClearsCriteria)
            .then(1)
            .otherwise(0)).as("normalClears")
        .sum(ConditionalOperators
            .when(masterModeClearsCriteria)
            .then(1)
            .otherwise(0)).as("masterClears");

    return Aggregation.newAggregation(userIdMatch, unwindRaids, groupByRaidName);
  }

  /**
   * Calculate user raid statistics based on the parsed data from a Discord option value. This
   * method returns a map of the raid stats grouped by the raid name.
   *
   * @param parsedData The parsed data needed to retrieve Raid Statistics for a player
   * @return Map of Raid Statistics grouped by raid name
   */
  public Flux<RaidStatistics> calculateRaidStats(UserChoiceValue parsedData) {
    Instant now = Instant.now(); // Timestamp for this action
    String userId = parsedData.getBungieDisplayName() + "#" + parsedData.getBungieDisplayCode();

    Mono<UserDetails> createAction = userRaidDetailsService.createUserDetails(now, parsedData)
        .doOnSubscribe(subscription -> log.info("Creation action initiated for user [{}]", userId))
        .doOnSuccess(userDetails -> log.info("Creation action finished for user [{}]", userId));

    Mono<UserDetails> updateAction = userRaidDetailsService.updateUserDetails(now, parsedData)
        .doOnSubscribe(subscription -> log.info("Update action initiated for user [{}]", userId))
        .doOnSuccess(userDetails -> log.info("Update action finished for user [{}]", userId));

    Aggregation aggregation = raidStatisticsAggregationPipeline(userId);

    return userRaidDetailsService.existsById(userId)
        .flatMap(exists -> exists ? updateAction : createAction)
        .flatMapMany(userDetails -> reactiveMongoTemplate.aggregate(aggregation,
            UserDetails.class, RaidStatistics.class));
  }
}
