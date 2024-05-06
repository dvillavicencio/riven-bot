package com.deahtstroke.rivenbot.service;

import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.dto.WeeklyActivity;
import com.deahtstroke.rivenbot.dto.destiny.manifest.DisplayProperties;
import com.deahtstroke.rivenbot.dto.destiny.manifest.ManifestResponseFields;
import com.deahtstroke.rivenbot.dto.destiny.milestone.ActivitiesDto;
import com.deahtstroke.rivenbot.dto.destiny.milestone.MilestoneEntry;
import com.deahtstroke.rivenbot.enums.ActivityMode;
import com.deahtstroke.rivenbot.enums.ManifestEntity;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class WeeklyActivitiesServiceTest {

  @Mock
  BungieAPIService bungieAPIService;

  @InjectMocks
  private WeeklyActivitiesService sut;

  @Test
  @DisplayName("Retrieve weekly raid works successfully")
  public void retrieveWeeklyRaidWorksSuccessfully() {
    // given: an activity mode
    ActivityMode activity = ActivityMode.RAID;

    var startTime = ZonedDateTime.now();
    var endTime = ZonedDateTime.now().plusDays(2L);
    var activitiesNoWeekly = List.of(new ActivitiesDto(1262462921L, Collections.emptyList()));
    var activitiesWeekly = List.of(new ActivitiesDto(2823159265L, List.of("897950155")));
    var milestoneResponse = Map.of(
        "526718853", new MilestoneEntry(526718853L,
            startTime, endTime, activitiesNoWeekly),

        "3618845105", new MilestoneEntry(3618845105L,
            startTime, endTime, activitiesWeekly),

        "2029743966", new MilestoneEntry(2029743966L,
            startTime, endTime, null)
    );

    when(bungieAPIService.getPublicMilestones())
        .thenReturn(Mono.just(milestoneResponse));

    var activityWithType = new ManifestResponseFields();
    activityWithType.setActivityTypeHash(608898761L);

    when(
        bungieAPIService.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 2823159265L))
        .thenReturn(Mono.just(activityWithType));

    var raidResponseFields = new ManifestResponseFields();
    raidResponseFields.setDisplayProperties(
        new DisplayProperties("someDescription", "Raid", null, null, false));

    when(bungieAPIService.getManifestEntity(ManifestEntity.ACTIVITY_TYPE_DEFINITION,
        608898761L))
        .thenReturn(Mono.just(raidResponseFields));

    var milestoneResponseFields = new ManifestResponseFields();
    var lastWishDisplayProperties = new DisplayProperties("Delve into the Last Wish raid",
        "The Last Wish", null, null, false);
    milestoneResponseFields.setDisplayProperties(lastWishDisplayProperties);

    when(
        bungieAPIService.getManifestEntity(ManifestEntity.MILESTONE_DEFINITION, 3618845105L))
        .thenReturn(Mono.just(milestoneResponseFields));

    WeeklyActivity expectedResponse = new WeeklyActivity("The Last Wish",
        "Delve into the Last Wish raid", startTime, endTime);

    // when: retrieve weekly activity is called
    Mono<WeeklyActivity> response = sut.getWeeklyActivity(activity);

    // then: the response matches the expected value
    StepVerifier.create(response)
        .expectNext(expectedResponse)
        .verifyComplete();
  }

  @Test
  @DisplayName("Retrieve weekly dungeon works successfully")
  public void retrieveWeeklyDungeonIsSuccessful() {
    // given: an activity mode
    ActivityMode activity = ActivityMode.DUNGEON;

    var startTime = ZonedDateTime.now();
    var endTime = ZonedDateTime.now().plusDays(2L);
    var activitiesNoWeekly = List.of(new ActivitiesDto(1262462921L, Collections.emptyList()));
    var activitiesWeekly = List.of(new ActivitiesDto(2823159265L, List.of("897950155")));
    var milestoneResponse = Map.of(
        "526718853", new MilestoneEntry(526718853L,
            startTime, endTime, activitiesNoWeekly),

        "3618845105", new MilestoneEntry(3618845105L,
            startTime, endTime, activitiesWeekly),

        "2029743966", new MilestoneEntry(2029743966L,
            startTime, endTime, null)
    );

    when(bungieAPIService.getPublicMilestones())
        .thenReturn(Mono.just(milestoneResponse));

    var activityWithType = new ManifestResponseFields();
    activityWithType.setActivityTypeHash(608898761L);

    when(bungieAPIService.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 2823159265L))
        .thenReturn(Mono.just(activityWithType));

    var dungeonResponseFields = new ManifestResponseFields();
    dungeonResponseFields.setDisplayProperties(
        new DisplayProperties("someDescription", "Dungeon", null, null, false));

    when(bungieAPIService.getManifestEntity(ManifestEntity.ACTIVITY_TYPE_DEFINITION,
        608898761L))
        .thenReturn(Mono.just(dungeonResponseFields));

    var milestoneResponseFields = new ManifestResponseFields();
    var dualityDisplayProperties = new DisplayProperties("Calus' mind as a dungeon lol",
        "Duality", null, null, false);
    milestoneResponseFields.setDisplayProperties(dualityDisplayProperties);

    when(bungieAPIService.getManifestEntity(ManifestEntity.MILESTONE_DEFINITION, 3618845105L))
        .thenReturn(Mono.just(milestoneResponseFields));

    WeeklyActivity expectedResponse = new WeeklyActivity(dualityDisplayProperties.getName(),
        dualityDisplayProperties.getDescription(), startTime, endTime);

    // when: retrieve weekly activity is called
    Mono<WeeklyActivity> response = sut.getWeeklyActivity(activity);

    // then: the response matches the expected value
    StepVerifier.create(response)
        .expectNext(expectedResponse)
        .verifyComplete();
  }

  @Test
  @DisplayName("Get weekly activity should throw an error if the criteria is not met")
  public void getWeeklyActivityError() {
    // given: an activity mode
    ActivityMode activity = ActivityMode.DUNGEON;

    var startTime = ZonedDateTime.now();
    var endTime = ZonedDateTime.now().plusDays(2L);
    var activitiesNoWeekly = List.of(new ActivitiesDto(1262462921L, Collections.emptyList()));
    var milestoneResponse = Map.of(
        "526718853", new MilestoneEntry(526718853L,
            startTime, endTime, activitiesNoWeekly),

        "2029743966", new MilestoneEntry(2029743966L,
            startTime, endTime, null)
    );

    when(bungieAPIService.getPublicMilestones())
        .thenReturn(Mono.just(milestoneResponse));

    // when: getWeeklyActivity is called
    var stepVerifier = StepVerifier.create(sut.getWeeklyActivity(activity));

    // then: a ResourceNotFoundException is thrown and the error message is correct
    stepVerifier.verifyErrorMessage(
        "No weekly activity found for activity type [%s]".formatted(activity));

    stepVerifier.expectError(ResourceNotFoundException.class).verify();
  }
}
