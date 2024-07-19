package com.deahtstroke.rivenbot.service;

import com.deahtstroke.rivenbot.entity.RaidStatistics;
import com.deahtstroke.rivenbot.entity.UserDetails;
import com.deahtstroke.rivenbot.enums.RaidDifficulty;
import com.deahtstroke.rivenbot.repository.UserDetailsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators.And;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.EvaluationOperators.Expr;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators.RegexMatch;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidStatsService {

  private static final Set<String> RAIDS_WITH_MASTER_MODE = Set.of(
      "Vault of Glass", "Vow of the Disciple", "King's Fall", "Root of Nightmares", "Crota's End",
      "Salvation's Edge"
  );
  private static final String USERNAME = "$username";
  private static final String USER_TAG = "$userTag";
  private static final String RAID_NAME = "userRaidDetails.raidName";
  private static final String IS_COMPLETED = "userRaidDetails.isCompleted";
  private static final String RAID_DIFFICULTY = "userRaidDetails.raidDifficulty";
  private static final String TOTAL_KILLS = "userRaidDetails.totalKills";
  private static final String TOTAL_DEATHS = "userRaidDetails.totalDeaths";
  private static final String FROM_BEGINNING = "userRaidDetails.fromBeginning";
  private static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private final UserDetailsRepository userDetailsRepository;
  private final UserDetailsService userRaidDetailsService;
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public RaidStatsService(
      UserDetailsService userRaidDetailsService,
      ReactiveMongoTemplate reactiveMongoTemplate,
      UserDetailsRepository userDetailsRepository) {
    this.userRaidDetailsService = userRaidDetailsService;
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.userDetailsRepository = userDetailsRepository;
  }

  private static Aggregation statsAggregationPipeline(String username, String userTag) {
    // Match username using case insensitivity and matching user tag
    RegexMatch usernameMatch = RegexMatch.valueOf(USERNAME).regex(username).options("i");
    Eq userTagMatch = Eq.valueOf(USER_TAG).equalToValue(userTag);
    MatchOperation userMatch = Aggregation.match(
        Expr.valueOf(And.and(usernameMatch, userTagMatch)));

    // Unwind the userRaidDetails array
    UnwindOperation unwindRaids = new UnwindOperation(Fields.field("userRaidDetails"));

    // All other calculations below for grouping up
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

    return Aggregation.newAggregation(userMatch, unwindRaids, groupByRaidName);
  }

  /**
   * Calculate user raid statistics based on the parsed data from a Discord option value. This
   * method returns a map of the raid stats grouped by the raid name.
   *
   * @param username       The username from Bungie
   * @param userTag        The userTag from Bungie
   * @param membershipId   The membership ID of the Destiny 2 user
   * @param membershipType The membership type of the Destiny 2 user
   * @return Map of Raid Statistics grouped by raid name
   */
  public Flux<RaidStatistics> calculateRaidStats(String username, String userTag,
      String membershipId, Integer membershipType) {
    Instant actionTimestamp = Instant.now(Clock.system(AMERICA_LOS_ANGELES));
    Aggregation aggregation = statsAggregationPipeline(username, userTag);
    return userDetailsRepository.existsByUsernameAndUserTag(username, userTag)
        .flatMap(exists -> Boolean.TRUE.equals(exists) ?
            updateUser(actionTimestamp, username, userTag, membershipType, membershipId) :
            createUser(actionTimestamp, username, userTag, membershipType, membershipId))
        .thenMany(
            reactiveMongoTemplate.aggregate(aggregation, UserDetails.class, RaidStatistics.class));
  }

  private Mono<Void> updateUser(Instant now, String username, String userTag,
      Integer membershipType, String membershipId) {
    return userRaidDetailsService.updateUserDetails(now, username, userTag, membershipId,
            membershipType)
        .doOnSubscribe(subscription -> log.info(
            "Update action initiated for user [{}] with ID [{}] and membership type: [{}]",
            username + "#" + userTag, membershipId, membershipType))
        .doOnSuccess(userDetails -> log.info(
            "Update action finished for user [{}] with ID: [{}] and membership type: [{}]",
            username + "#" + userTag, membershipId, membershipType));
  }

  private Mono<Void> createUser(Instant now, String username, String userTag,
      Integer membershipType, String membershipId) {
    return userRaidDetailsService.createUserDetails(now, username, userTag, membershipId,
            membershipType)
        .doOnSubscribe(subscription -> log.info(
            "Creation action initiated for user [{}] with ID: [{}] and membership type: [{}]",
            username + "#" + userTag, membershipId, membershipType))
        .doOnSuccess(userDetails -> log.info(
            "Creation action finished for user [{}] with ID: [{}] and membership type: [{}]",
            username + "#" + userTag, membershipId, membershipType));
  }
}
