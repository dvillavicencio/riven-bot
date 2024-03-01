package com.danielvm.destiny2bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.ActivitiesResponse;
import com.danielvm.destiny2bot.dto.destiny.Activity;
import com.danielvm.destiny2bot.dto.destiny.ActivityDetails;
import com.danielvm.destiny2bot.dto.destiny.Basic;
import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.ValueEntry;
import com.danielvm.destiny2bot.dto.destiny.characters.Characters;
import com.danielvm.destiny2bot.dto.destiny.characters.CharactersResponse;
import com.danielvm.destiny2bot.dto.destiny.characters.UserCharacter;
import com.danielvm.destiny2bot.dto.destiny.manifest.DisplayProperties;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import com.danielvm.destiny2bot.entity.PGCRDetails;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.entity.UserRaidDetails;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import com.danielvm.destiny2bot.enums.RaidDifficulty;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
public class UserRaidDetailsServiceTest {

  @Mock
  BungieClient bungieClient;
  @Mock
  BungieClientWrapper bungieClientWrapper;
  @Mock
  UserDetailsRepository userDetailsRepository;
  @Mock
  PostGameCarnageService postGameCarnageService;

  @InjectMocks
  UserRaidDetailsService sut;

  @Test
  @DisplayName("Get all activities works successfully for characters with only one page")
  public void getUserActivitiesSuccess() {
    // given: membershipType, membershipId, and characterId
    Integer membershipType = 3;
    String membershipId = "1389012";
    String characterId = "1";

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    ArrayList<Activity> activities = new ArrayList<>();
    for (int i = 0; i < 249; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      activities.add(new Activity(completionDate, details, entryMap));
    }
    var activitiesResponse = new ActivitiesResponse(activities);
    var bungieResponse = new BungieResponse<>(activitiesResponse);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(bungieResponse));

    // when: getActivitiesAll is called
    var response = sut.getActivitiesAll(membershipType, membershipId, characterId);

    // then: the response that is returned has the correct size
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list).isNotNull();
          assertThat(list.size()).isEqualTo(249);
        }).verifyComplete();

    // and: the bungie API calls is only one
    verify(bungieClient, times(1)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 0);
    verify(bungieClient, times(0)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 1);
  }

  @Test
  @DisplayName("Get all activities works successfully for characters with more than one page")
  public void getUserActivitiesMoreThanOnePageSuccess() {
    // given: membershipType, membershipId, and characterId
    Integer membershipType = 3;
    String membershipId = "1389012";
    String characterId = "1";

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    ArrayList<Activity> firstActivities = new ArrayList<>();
    for (int i = 0; i < 250; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      firstActivities.add(new Activity(completionDate, details, entryMap));
    }
    var firstPageActivities = new ActivitiesResponse(firstActivities);
    var firstPage = new BungieResponse<>(firstPageActivities);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(firstPage));

    ArrayList<Activity> secondActivities = new ArrayList<>();
    for (int i = 0; i < 249; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      secondActivities.add(new Activity(completionDate, details, entryMap));
    }
    var secondPageActivities = new ActivitiesResponse(secondActivities);
    var secondPage = new BungieResponse<>(secondPageActivities);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 1))
        .thenReturn(Mono.just(secondPage));

    // when: getActivitiesAll is called
    var response = sut.getActivitiesAll(membershipType, membershipId, characterId);

    // then: the response that is returned has the correct size
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list).isNotNull();
          assertThat(list.size()).isEqualTo(499);
        }).verifyComplete();

    // and: there's two Bungie API calls
    verify(bungieClient, times(1)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 0);
    verify(bungieClient, times(1)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 1);
    verify(bungieClient, times(0)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 2);
  }

  @Test
  @DisplayName("Get all activities returns an empty list for characters with no raids")
  public void getUserActivitiesEmptyActivityHistorySuccess() {
    // given: membershipType, membershipId, and characterId
    Integer membershipType = 3;
    String membershipId = "1389012";
    String characterId = "1";

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );

    var bungieResponse = new BungieResponse<ActivitiesResponse>(null);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(bungieResponse));

    // when: getActivitiesAll is called
    var response = StepVerifier.create(
        sut.getActivitiesAll(membershipType, membershipId, characterId));

    // then: the response emits no values
    response.expectNextCount(0).verifyComplete();

    // and: the bungie API calls is only one
    verify(bungieClient, times(1)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 0);
    verify(bungieClient, times(0)).getActivityHistory(membershipType, membershipId, characterId,
        250, 4, 1);
  }

  @Test
  @DisplayName("create action for a new user is successful")
  public void createUserRaidDetailsIsSuccessful() {
    // given: parsed data for user details
    String username = "Deaht";
    Integer userTag = 8080;
    String membershipId = "12345";
    Integer membershipType = 3;
    String clanName = "Legends of Honor";
    String userId = username + "#" + userTag;
    Instant creationInstant = Instant.now();
    UserChoiceValue parsedData = new UserChoiceValue(membershipId, membershipType, username,
        userTag, "Legends of Honor");

    Map<String, UserCharacter> data = Map.of("1", new UserCharacter());
    Characters characters = new Characters(data);
    CharactersResponse charactersResponse = new CharactersResponse(characters);
    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(new BungieResponse<>(charactersResponse)));

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    List<Activity> activities = List.of(
        new Activity(Instant.now(), new ActivityDetails(1L, 789120L, 4), entryMap),
        new Activity(Instant.now(), new ActivityDetails(1L, 789121L, 4), entryMap),
        new Activity(Instant.now(), new ActivityDetails(1L, 789122L, 4), entryMap),
        new Activity(Instant.now(), new ActivityDetails(2L, 789123L, 4), entryMap),
        new Activity(Instant.now(), new ActivityDetails(2L, 789124L, 4), entryMap)
    );
    ActivitiesResponse activitiesResponse = new ActivitiesResponse(activities);
    when(bungieClient.getActivityHistory(membershipType, membershipId, "1", 250, 4, 0))
        .thenReturn(Mono.just(new BungieResponse<>(activitiesResponse)));

    ResponseFields firstActivity = ResponseFields.builder()
        .displayProperties(new DisplayProperties("", "Last Wish: 50", "", "", false))
        .build();
    when(bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 1L))
        .thenReturn(Mono.just(new BungieResponse<>(firstActivity)));

    ResponseFields secondActivity = ResponseFields.builder()
        .displayProperties(new DisplayProperties("", "King's Fall: Master", "", "", false))
        .build();
    when(bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 2L))
        .thenReturn(Mono.just(new BungieResponse<>(secondActivity)));

    PGCRDetails pgcr = new PGCRDetails(null, true, null);
    when(postGameCarnageService.retrievePGCR(any(Long.class)))
        .thenReturn(Mono.just((pgcr)));

    when(userDetailsRepository.save(assertArg(ud -> {
      UserRaidDetails lastWish = ud.getUserRaidDetails().stream()
          .filter(raid -> raid.getInstanceId() == 789120L)
          .findFirst().orElse(null);

      UserRaidDetails kingsFall = ud.getUserRaidDetails().stream()
          .filter(raid -> raid.getInstanceId() == 789124L)
          .findFirst().orElse(null);

      assertThat(ud.getUserIdentifier()).isEqualTo(userId);
      assertThat(ud.getLastRequestDateTime()).isEqualTo(creationInstant);
      assertThat(ud.getDestinyClanName()).isEqualTo(clanName);
      assertThat(ud.getUserRaidDetails().size()).isEqualTo(5);
      assertThat(lastWish.getRaidName()).isEqualTo("Last Wish");
      assertThat(lastWish.getRaidDifficulty()).isNull();
      assertThat(lastWish.getIsCompleted()).isTrue();
      assertThat(lastWish.getTotalKills()).isEqualTo(134);
      assertThat(lastWish.getTotalDeaths()).isEqualTo(0);
      assertThat(lastWish.getDurationSeconds()).isEqualTo(3600);
      assertThat(kingsFall.getRaidName()).isEqualTo("King's Fall");
      assertThat(kingsFall.getRaidDifficulty()).isEqualTo(RaidDifficulty.MASTER);
    }))).thenReturn(Mono.empty());

    // when: create user details is called
    var response = StepVerifier.create(sut.createUserDetails(creationInstant, parsedData));

    // then: the saved entity is saved correctly
    response.verifyComplete();

    // and: the correct interactions occur
    verify(bungieClient, times(1)).getUserCharacters(membershipType, membershipId);
    verify(bungieClientWrapper, times(5)).getManifestEntity(any(), anyLong());
    verify(postGameCarnageService, atMost(5)).retrievePGCR(anyLong());
    verify(userDetailsRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("Raids missing attributes such as deaths, kills, etc. should be defaulted to zero")
  public void createUserDetailsRaidStatsDefaultValues() {
    // given: parsed data for user details
    String username = "Deaht";
    Integer userTag = 8080;
    String membershipId = "12345";
    Integer membershipType = 3;
    String clanName = "Legends of Honor";
    String userId = username + "#" + userTag;
    Instant creationInstant = Instant.now();
    UserChoiceValue parsedData = new UserChoiceValue(membershipId, membershipType, username,
        userTag, "Legends of Honor");

    Map<String, UserCharacter> data = Map.of("1", new UserCharacter());
    Characters characters = new Characters(data);
    CharactersResponse charactersResponse = new CharactersResponse(characters);
    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(new BungieResponse<>(charactersResponse)));

    List<Activity> activities = List.of(
        new Activity(Instant.now(), new ActivityDetails(1L, 789120L, 4), Collections.emptyMap()),
        new Activity(Instant.now(), new ActivityDetails(1L, 789121L, 4), Collections.emptyMap()),
        new Activity(Instant.now(), new ActivityDetails(1L, 789122L, 4), Collections.emptyMap()),
        new Activity(Instant.now(), new ActivityDetails(2L, 789123L, 4), Collections.emptyMap()),
        new Activity(Instant.now(), new ActivityDetails(2L, 789124L, 4), Collections.emptyMap())
    );
    ActivitiesResponse activitiesResponse = new ActivitiesResponse(activities);
    when(bungieClient.getActivityHistory(membershipType, membershipId, "1", 250, 4, 0))
        .thenReturn(Mono.just(new BungieResponse<>(activitiesResponse)));

    ResponseFields firstActivity = ResponseFields.builder()
        .displayProperties(new DisplayProperties("", "Last Wish: 50", "", "", false))
        .build();
    when(bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 1L))
        .thenReturn(Mono.just(new BungieResponse<>(firstActivity)));

    ResponseFields secondActivity = ResponseFields.builder()
        .displayProperties(new DisplayProperties("", "King's Fall: Master", "", "", false))
        .build();
    when(bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 2L))
        .thenReturn(Mono.just(new BungieResponse<>(secondActivity)));

    PGCRDetails pgcr = new PGCRDetails(null, true, null);
    when(postGameCarnageService.retrievePGCR(any(Long.class)))
        .thenReturn(Mono.just((pgcr)));

    when(userDetailsRepository.save(assertArg(ud -> {
      UserRaidDetails lastWish = ud.getUserRaidDetails().stream()
          .filter(raid -> raid.getInstanceId() == 789120L)
          .findFirst().orElse(null);

      UserRaidDetails kingsFall = ud.getUserRaidDetails().stream()
          .filter(raid -> raid.getInstanceId() == 789124L)
          .findFirst().orElse(null);

      assertThat(ud.getUserIdentifier()).isEqualTo(userId);
      assertThat(ud.getLastRequestDateTime()).isEqualTo(creationInstant);
      assertThat(ud.getDestinyClanName()).isEqualTo(clanName);
      assertThat(ud.getUserRaidDetails().size()).isEqualTo(5);
      assertThat(lastWish.getRaidName()).isEqualTo("Last Wish");
      assertThat(lastWish.getRaidDifficulty()).isNull();
      assertThat(lastWish.getIsCompleted()).isFalse();
      assertThat(lastWish.getTotalKills()).isEqualTo(0);
      assertThat(lastWish.getTotalDeaths()).isEqualTo(0);
      assertThat(lastWish.getDurationSeconds()).isEqualTo(0);
      assertThat(kingsFall.getRaidName()).isEqualTo("King's Fall");
      assertThat(kingsFall.getRaidDifficulty()).isEqualTo(RaidDifficulty.MASTER);
    }))).thenReturn(Mono.empty());

    // when: create user details is called
    var response = StepVerifier.create(sut.createUserDetails(creationInstant, parsedData));

    // then: the saved entity is saved correctly
    response.verifyComplete();

    // and: the correct interactions occur
    verify(bungieClient, times(1)).getUserCharacters(membershipType, membershipId);
    verify(bungieClientWrapper, times(5)).getManifestEntity(any(), anyLong());
    verify(postGameCarnageService, atMost(5)).retrievePGCR(anyLong());
    verify(userDetailsRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("Get all characters activities until works for updating user raid details")
  public void getCharacterActivitiesUntil() {
    // given: membershipType, membershipId, characterId, and a timestamp
    Integer membershipType = 3;
    String membershipId = "SomeId";
    String characterId = "1893";
    Instant timestamp = LocalDate.now().minusDays(25).atStartOfDay().toInstant(ZoneOffset.UTC);

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    ArrayList<Activity> activities = new ArrayList<>();
    for (int i = 0; i < 125; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      activities.add(new Activity(completionDate, details, entryMap));
    }
    var activitiesResponse = new ActivitiesResponse(activities);
    var bungieResponse = new BungieResponse<>(activitiesResponse);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(bungieResponse));

    // when: getting activities and the most recent call was 25 days ago
    var response = sut.getActivitiesUntil(membershipType, membershipId, characterId, timestamp);

    // then: we only get the 25 most-recent activities
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list.size()).isEqualTo(25);
        }).verifyComplete();
  }

  @Test
  @DisplayName("Get all characters activities until works for more than one page of information")
  public void getCharacterActivitiesUntilVariousPages() {
    // given: membershipType, membershipId, characterId, and a timestamp
    Integer membershipType = 3;
    String membershipId = "SomeId";
    String characterId = "1893";
    // last request for this user was made 300 days ago
    Instant timestamp = LocalDate.now().minusDays(300)
        .atStartOfDay().toInstant(ZoneOffset.UTC);

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    ArrayList<Activity> firstPageActivities = new ArrayList<>();
    for (int i = 0; i < 250; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      firstPageActivities.add(new Activity(completionDate, details, entryMap));
    }
    var activitiesResponse = new ActivitiesResponse(firstPageActivities);
    var firstPage = new BungieResponse<>(activitiesResponse);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(firstPage));

    ArrayList<Activity> secondPageActivities = new ArrayList<>();
    for (int i = 250; i < 500; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      secondPageActivities.add(new Activity(completionDate, details, entryMap));
    }
    var secondResponse = new ActivitiesResponse(secondPageActivities);
    var secondPage = new BungieResponse<>(secondResponse);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 1))
        .thenReturn(Mono.just(secondPage));

    // when: getting activities and the most recent call was 300 days ago
    var response = sut.getActivitiesUntil(membershipType, membershipId, characterId, timestamp);

    // then: we only get the 300 most-recent activities from two pages
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list).isNotNull();
          assertThat(list.size()).isEqualTo(300);
          assertThat(
              list.stream().allMatch(activity -> activity.getPeriod().isAfter(timestamp))).isTrue();
        }).verifyComplete();

    // and: verify that the activities endpoint was called for both pages and a third page was not called
    verify(bungieClient, times(1))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0);
    verify(bungieClient, times(1))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 1);
    verify(bungieClient, times(0))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 2);
  }

  @Test
  @DisplayName("Get activities until works when there's no new activities for a user")
  public void getCharacter() {
    // given: membershipType, membershipId, characterId, and a timestamp
    Integer membershipType = 3;
    String membershipId = "SomeId";
    String characterId = "1893";
    Instant timestamp = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    ArrayList<Activity> firstPageActivities = new ArrayList<>();
    for (int i = 0; i < 250; i++) {
      ActivityDetails details = new ActivityDetails(1L, (long) i, 4);
      Instant completionDate = LocalDate.now().minusDays(i).atStartOfDay()
          .toInstant(ZoneOffset.UTC);
      firstPageActivities.add(new Activity(completionDate, details, entryMap));
    }
    var activitiesResponse = new ActivitiesResponse(firstPageActivities);
    var firstPage = new BungieResponse<>(activitiesResponse);
    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0))
        .thenReturn(Mono.just(firstPage));

    // when: getting activities and the most recent call was 300 days ago
    var response = sut.getActivitiesUntil(membershipType, membershipId, characterId, timestamp);

    // then: we get zero new raid entries
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list).isNotNull();
          assertThat(list.size()).isEqualTo(0);
        }).verifyComplete();

    // and: verify that the activities endpoint was called for only the first page
    verify(bungieClient, times(1))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0);
    verify(bungieClient, times(0))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 1);
  }

  @Test
  @DisplayName("Update action is successful for updating user raids with latest information")
  public void updateActionSuccessful() {
    // given: the timestamp this action was triggered and parsed user data
    String username = "Deaht";
    Integer userTag = 8080;
    String membershipId = "12345";
    Integer membershipType = 3;
    String clanName = "Legends of Honor";
    String userId = username + "#" + userTag;
    var updatedInstant = Instant.now();
    var parsedData = new UserChoiceValue(membershipId, membershipType, username, userTag, clanName);

    var threeDaysAgo = LocalDate.now().minusDays(3).atStartOfDay().toInstant(ZoneOffset.UTC);
    List<UserRaidDetails> existingData = new ArrayList<>();
    existingData.add(new UserRaidDetails("Last Wish", null, null, true, 1000, 3, 34.0,
        3600, true, 1L));
    existingData.add(
        new UserRaidDetails("King's Fall", RaidDifficulty.NORMAL, null, true, 1000, 3, 34.0,
            3600, true, 2L));
    existingData.add(
        new UserRaidDetails("King's Fall", RaidDifficulty.MASTER, null, true, 1000, 3, 34.0,
            3600, true, 3L));
    existingData.add(
        new UserRaidDetails("Vow of the Disciple", RaidDifficulty.NORMAL, null, false, 0, 0, 31.1,
            333, true, 4L));

    // Last time this user was searched for was three days ago
    UserDetails existingUser = new UserDetails(userId, clanName, threeDaysAgo, existingData);
    when(userDetailsRepository.findById(userId)).thenReturn(Mono.just(existingUser));

    Map<String, UserCharacter> data = Map.of("1", new UserCharacter());
    Characters characters = new Characters(data);
    CharactersResponse charactersResponse = new CharactersResponse(characters);
    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(new BungieResponse<>(charactersResponse)));

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    Instant today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    List<Activity> activities = List.of(
        new Activity(today, new ActivityDetails(1L, 5L, 4), entryMap),
        new Activity(threeDaysAgo, new ActivityDetails(1L, 1L, 4), Collections.emptyMap()),
        new Activity(threeDaysAgo, new ActivityDetails(1L, 2L, 4), Collections.emptyMap()),
        new Activity(threeDaysAgo, new ActivityDetails(2L, 3L, 4), Collections.emptyMap()),
        new Activity(threeDaysAgo, new ActivityDetails(2L, 4L, 4), Collections.emptyMap())
    );
    ActivitiesResponse activitiesResponse = new ActivitiesResponse(activities);
    when(bungieClient.getActivityHistory(membershipType, membershipId, "1", 250, 4, 0))
        .thenReturn(Mono.just(new BungieResponse<>(activitiesResponse)));

    ResponseFields firstActivity = ResponseFields.builder()
        .displayProperties(new DisplayProperties("", "Last Wish: 50", "", "", false))
        .build();
    when(bungieClientWrapper.getManifestEntity(ManifestEntity.ACTIVITY_DEFINITION, 1L))
        .thenReturn(Mono.just(new BungieResponse<>(firstActivity)));

    PGCRDetails pgcr = new PGCRDetails(null, true, null);
    when(postGameCarnageService.retrievePGCR(any(Long.class)))
        .thenReturn(Mono.just((pgcr)));

    when(userDetailsRepository.save(assertArg(ud -> {
      UserRaidDetails lastWish = ud.getUserRaidDetails().stream()
          .filter(raid -> raid.getInstanceId() == 5L)
          .findFirst().orElse(null);

      assertThat(ud.getUserIdentifier()).isEqualTo(userId);
      assertThat(ud.getLastRequestDateTime()).isEqualTo(updatedInstant);
      assertThat(ud.getDestinyClanName()).isEqualTo(clanName);
      assertThat(ud.getUserRaidDetails().size()).isEqualTo(5);
      assertThat(lastWish.getRaidName()).isEqualTo("Last Wish");
      assertThat(lastWish.getRaidDifficulty()).isNull();
      assertThat(lastWish.getIsCompleted()).isTrue();
      assertThat(lastWish.getTotalKills()).isEqualTo(134);
      assertThat(lastWish.getTotalDeaths()).isEqualTo(0);
      assertThat(lastWish.getDurationSeconds()).isEqualTo(3600);
    }))).thenReturn(Mono.empty());

    // when: create user details is called
    var response = StepVerifier.create(sut.updateUserDetails(updatedInstant, parsedData));

    // then: the saved entity is saved correctly
    response.verifyComplete();

    // and: the correct interactions occur
    verify(bungieClient, times(1)).getUserCharacters(membershipType, membershipId);
    verify(bungieClientWrapper, times(1)).getManifestEntity(any(), anyLong());
    verify(postGameCarnageService, atMost(1)).retrievePGCR(anyLong());
    verify(userDetailsRepository, times(1)).save(any());
  }
}
