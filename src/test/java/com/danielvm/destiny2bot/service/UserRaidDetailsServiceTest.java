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
  @DisplayName("Retrieve Raid Stats makes a create action for a new user")
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

    // when: getting activities and the most recent call was 25 days ago
    var response = sut.getActivitiesUntil(membershipType, membershipId, characterId, timestamp);

    // then: we only get the 25 most-recent activities
    StepVerifier.create(response.collectList())
        .assertNext(list -> {
          assertThat(list).isNotNull();
          assertThat(list.size()).isEqualTo(300);
        }).verifyComplete();

    // and: verify that the activities endpoint was called for both pages
    verify(bungieClient, times(1))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 0);
    verify(bungieClient, times(1))
        .getActivityHistory(membershipType, membershipId, characterId, 250, 4, 1);
  }
}
