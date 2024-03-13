package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.ManifestEntity.ACTIVITY_DEFINITION;
import static com.danielvm.destiny2bot.enums.ManifestEntity.ACTIVITY_TYPE_DEFINITION;
import static com.danielvm.destiny2bot.enums.ManifestEntity.MILESTONE_DEFINITION;

import com.danielvm.destiny2bot.dto.MilestoneResponse;
import com.danielvm.destiny2bot.dto.WeeklyActivity;
import com.danielvm.destiny2bot.dto.destiny.manifest.ManifestResponseFields;
import com.danielvm.destiny2bot.dto.destiny.milestone.ActivitiesDto;
import com.danielvm.destiny2bot.dto.destiny.milestone.MilestoneEntry;
import com.danielvm.destiny2bot.enums.ActivityMode;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WeeklyActivitiesService {

  private final BungieAPIService bungieAPIService;

  public WeeklyActivitiesService(BungieAPIService bungieAPIService) {
    this.bungieAPIService = bungieAPIService;
  }

  /**
   * Fetches a weekly activity based on the activity mode
   *
   * @param activityMode The type of the weekly activity (see {@link ActivityMode})
   * @return {@link MilestoneResponse}
   */
  public Mono<WeeklyActivity> getWeeklyActivity(ActivityMode activityMode) {
    return bungieAPIService.getPublicMilestones()
        .flatMapIterable(Map::values)
        .filter(this::hasWeeklyObjectives)
        .filterWhen(milestoneEntry -> activityModeMatches(milestoneEntry, activityMode))
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
            "No weekly activity found for activity type [%s]".formatted(activityMode))))
        .flatMap(this::createWeeklyActivity)
        .next(); // ideally there should only be one weekly activity

  }

  private Mono<WeeklyActivity> createWeeklyActivity(MilestoneEntry milestoneEntry) {
    return bungieAPIService.getManifestEntity(MILESTONE_DEFINITION,
            milestoneEntry.getMilestoneHash())
        .map(ManifestResponseFields::getDisplayProperties)
        .map(displayProperties ->
            WeeklyActivity.builder()
                .name(displayProperties.getName())
                .description(displayProperties.getDescription())
                .startDate(milestoneEntry.getStartDate())
                .endDate(milestoneEntry.getEndDate())
                .build());
  }

  private Mono<Boolean> activityModeMatches(MilestoneEntry entry, ActivityMode activityMode) {
    if (CollectionUtils.isEmpty(entry.getActivities())) {
      return Mono.just(false);
    }
    return Flux.fromIterable(entry.getActivities())
        .flatMap(activity -> bungieAPIService.getManifestEntity(
            ACTIVITY_DEFINITION, activity.getActivityHash()))
        .filter(activity -> activity.getActivityTypeHash() != null)
        .flatMap(activityDefinition -> bungieAPIService.getManifestEntity(
            ACTIVITY_TYPE_DEFINITION, activityDefinition.getActivityTypeHash()))
        .filter(activityType -> activityType != null &&
                                activityType.getDisplayProperties() != null)
        .any(activityTypeDefinition -> activityTypeDefinition.getDisplayProperties().getName()
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
