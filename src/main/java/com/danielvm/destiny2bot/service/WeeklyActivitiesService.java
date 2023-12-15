package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ACTIVITY_DEFINITION;
import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ACTIVITY_TYPE_DEFINITION;
import static com.danielvm.destiny2bot.enums.EntityTypeEnum.MILESTONE_DEFINITION;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.dto.MilestoneResponse;
import com.danielvm.destiny2bot.dto.WeeklyActivity;
import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.milestone.ActivitiesDto;
import com.danielvm.destiny2bot.dto.destiny.milestone.MilestoneEntry;
import com.danielvm.destiny2bot.enums.ActivityModeEnum;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WeeklyActivitiesService {

  private final BungieClient bungieClient;
  private final BungieClientWrapper bungieClientWrapper;

  public WeeklyActivitiesService(
      BungieClient bungieClient,
      BungieClientWrapper bungieClientWrapper) {
    this.bungieClient = bungieClient;
    this.bungieClientWrapper = bungieClientWrapper;
  }

  /**
   * Fetches a weekly activity
   *
   * @param activityMode The type of the weekly activity
   * @return {@link MilestoneResponse}
   */
  public Mono<WeeklyActivity> getWeeklyActivity(ActivityModeEnum activityMode) {
    return bungieClient.getPublicMilestonesRx()
        .map(GenericResponse::getResponse)
        .flatMapIterable(Map::values)
        .filter(this::hasWeeklyObjectives)
        .filterWhen(m -> activityModeMatches(m, activityMode))
        .flatMap(m -> bungieClientWrapper.getManifestEntityRx(
                MILESTONE_DEFINITION, m.getMilestoneHash())
            .map(me -> {
              var displayProperties = me.getResponse().getDisplayProperties();
              return WeeklyActivity.builder()
                  .name(displayProperties.getName())
                  .description(displayProperties.getDescription())
                  .startDate(m.getStartDate())
                  .endDate(m.getEndDate())
                  .build();
            }))
        .next() // ideally there should only be one weekly activity
        .switchIfEmpty(Mono.error(
            new ResourceNotFoundException("No weekly activity found for activity type [%s]"
                .formatted(activityMode))));
  }

  private Mono<Boolean> activityModeMatches(MilestoneEntry e, ActivityModeEnum activityMode) {
    if (Objects.isNull(e) || CollectionUtils.isEmpty(e.getActivities())) {
      return Mono.just(false);
    }
    return Flux.fromIterable(e.getActivities())
        .flatMap(
            a -> bungieClientWrapper.getManifestEntityRx(
                ACTIVITY_DEFINITION, a.getActivityHash()))
        .filter(ad -> Objects.nonNull(ad.getResponse().getActivityTypeHash()))
        .flatMap(ad ->
            bungieClientWrapper.getManifestEntityRx(
                ACTIVITY_TYPE_DEFINITION, ad.getResponse().getActivityTypeHash()))
        .filter(at -> Objects.nonNull(at.getResponse()) && Objects.nonNull(
            at.getResponse().getDisplayProperties()))
        .any(at -> at.getResponse().getDisplayProperties().getName()
            .equalsIgnoreCase(activityMode.getLabel()));
  }

  private boolean hasWeeklyObjectives(MilestoneEntry entry) {
    if (CollectionUtils.isEmpty(entry.getActivities())) {
      return false;
    }
    return entry.getActivities().stream()
        .map(ActivitiesDto::getChallengeObjectiveHashes)
        .flatMap(List::stream)
        .findAny().isPresent();
  }
}
