package com.deahtstroke.rivenbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.dto.destiny.ActivitiesResponse;
import com.deahtstroke.rivenbot.dto.destiny.Activity;
import com.deahtstroke.rivenbot.dto.destiny.ActivityDetails;
import com.deahtstroke.rivenbot.dto.destiny.Basic;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.SearchResult;
import com.deahtstroke.rivenbot.dto.destiny.UserGlobalSearchBody;
import com.deahtstroke.rivenbot.dto.destiny.UserSearchResult;
import com.deahtstroke.rivenbot.dto.destiny.ValueEntry;
import com.deahtstroke.rivenbot.dto.destiny.characters.Characters;
import com.deahtstroke.rivenbot.dto.destiny.characters.CharactersResponse;
import com.deahtstroke.rivenbot.dto.destiny.characters.UserCharacter;
import com.deahtstroke.rivenbot.dto.destiny.manifest.ManifestResponseFields;
import com.deahtstroke.rivenbot.dto.destiny.milestone.MilestoneEntry;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.ManifestEntity;
import com.deahtstroke.rivenbot.exception.ManifestEntityNotFoundException;
import com.deahtstroke.rivenbot.exception.NoCharactersFoundException;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BungieAPIServiceTest {

  static MockWebServer mockWebServer;

  static ObjectMapper objectMapper = new JsonMapper();

  @Mock
  BungieClient bungieClient;

  @InjectMocks
  BungieAPIService sut;

  @BeforeAll
  static void setup() throws IOException {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  static Stream<Arguments> emptyRaidActivities() {
    return Stream.of(
        Arguments.of(BungieResponse.of(null)),
        Arguments.of(BungieResponse.of(new ActivitiesResponse(Collections.emptyList())))
    );
  }

  @BeforeEach
  void beforeEach() {
    WebClient webClient = WebClient.builder()
        .baseUrl("http://localhost:" + mockWebServer.getPort())
        .build();
    this.sut = new BungieAPIService(this.bungieClient, webClient);
  }

  @Test
  @DisplayName("Get raid activities works successfully")
  void shouldGetRaidActivitiesWorkSuccessfully() {
    // given: membershipType, membershipId, characterId and pageNumber
    Integer membershipType = 3;
    String membershipId = "12301312312";
    String characterId = "801983071";
    Integer pageNumber = 0;

    Map<String, ValueEntry> entryMap = Map.of(
        "deaths", new ValueEntry("deaths", new Basic(0.0, "0")),
        "killsDeathsAssists", new ValueEntry("killsDeathsAssists", new Basic(134.0, "134")),
        "activityDurationSeconds",
        new ValueEntry("activityDurationSeconds", new Basic(3600.0, "3600")),
        "completed", new ValueEntry("completed", new Basic(1.0, "1.0")),
        "kills", new ValueEntry("kills", new Basic(134.0, "134.0"))
    );
    List<Activity> activities = List.of(
        new Activity(Instant.now(), new ActivityDetails(1L, 1234L, 4), entryMap));

    BungieResponse<ActivitiesResponse> activitiesResponse = BungieResponse.of(
        new ActivitiesResponse(activities));

    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId,
        250, 4, pageNumber)).thenReturn(Mono.just(activitiesResponse));

    // when: getRaidActivities is invoked
    // then: the response contains the needed data
    StepVerifier.create(
            sut.getRaidActivities(membershipType, membershipId, characterId, pageNumber))
        .assertNext(response -> {
          assertThat(response.getActivities()).hasSize(1);
          assertThat(response.getActivities()).containsAll(activities);
        })
        .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("emptyRaidActivities")
  @DisplayName("Get raid activities should return an empty response if condition is met")
  void shouldReturnEmptyActivitiesResponse(BungieResponse<ActivitiesResponse> activitiesResponse) {
    // given: membershipType, membershipId, characterId and pageNumber
    Integer membershipType = 3;
    String membershipId = "12301312312";
    String characterId = "801983071";
    Integer pageNumber = 0;

    when(bungieClient.getActivityHistory(membershipType, membershipId, characterId,
        250, 4, pageNumber)).thenReturn(Mono.just(activitiesResponse));

    // when: getRaidActivities is invoked
    // then: the response is mapped to an empty Bungie Response with no activities
    StepVerifier.create(
            sut.getRaidActivities(membershipType, membershipId, characterId, pageNumber))
        .assertNext(response -> assertThat(response.getActivities()).isEmpty())
        .verifyComplete();
  }

  @Test
  @DisplayName("Get user characters should work successfully")
  void shouldWorkSuccessfully() {
    // given: membershipType and membershipId
    Integer membershipType = 1;
    String membershipId = "123107401923";

    String characterId1 = "189401231231";
    String characterId2 = "5898u1023806";
    Map<String, UserCharacter> data = Map.of(
        characterId1, new UserCharacter(membershipId, membershipType, characterId1, 2000, 2, 1, 2),
        characterId2, new UserCharacter(membershipId, membershipType, characterId2, 2000, 1, 1, 2)
    );
    BungieResponse<CharactersResponse> charactersResponse = BungieResponse.of(
        new CharactersResponse(new Characters(data)));
    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(charactersResponse));

    // when: getUserCharacters is invoked
    // then: the returned map of characters has the correct size and the correct entries
    StepVerifier.create(sut.getUserCharacters(membershipType, membershipId))
        .assertNext(characters -> {
          assertThat(characters).isNotNull();
          assertThat(characters).hasSize(2);
          assertThat(characters.entrySet()).containsAll(characters.entrySet());
        });
  }

  @Test
  @DisplayName("Get user character should throw an appropriate exception when characters are missing")
  void shouldThrowExceptionOnMissingCharacters() {
    // given: membershipType and membershipId
    Integer membershipType = 1;
    String membershipId = "123107401923";
    Map<String, UserCharacter> data = Collections.emptyMap();
    BungieResponse<CharactersResponse> charactersResponse = BungieResponse.of(
        new CharactersResponse(new Characters(data)));

    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(charactersResponse));

    // when: getUserCharacters is invoked
    // then: the returned map of characters has the correct size and the correct entries
    StepVerifier.create(sut.getUserCharacters(membershipType, membershipId))
        .verifyErrorSatisfies(error -> {
          assertThat(error).isInstanceOf(NoCharactersFoundException.class);
          assertThat(error.getMessage()).isEqualTo(
              "No characters found for user with ID [%s] and membership type [%s]".formatted(
                  membershipId, membershipType));
          assertThat(((NoCharactersFoundException) error).getErrorInteractionResponse()).isEqualTo(
              InteractionResponseData.builder()
                  .content(
                      "It seems that the user you were trying to find does not have any Destiny 2 characters")
                  .build());
        });
  }

  @Test
  @DisplayName("Get user character should throw an appropriate exception when error code is other than 1")
  void shouldThrowExceptionOnErrorCodeOtherThan1() {
    // given: membershipType and membershipId
    Integer membershipType = 1;
    String membershipId = "123107401923";
    Map<String, UserCharacter> data = Collections.emptyMap();
    BungieResponse<CharactersResponse> charactersResponse = new BungieResponse<>(
        new CharactersResponse(new Characters(data)), 217);

    when(bungieClient.getUserCharacters(membershipType, membershipId))
        .thenReturn(Mono.just(charactersResponse));

    // when: getUserCharacters is invoked
    // then: the returned map of characters has the correct size and the correct entries
    StepVerifier.create(sut.getUserCharacters(membershipType, membershipId))
        .verifyErrorSatisfies(error -> {
          assertThat(error).isInstanceOf(NoCharactersFoundException.class);
          assertThat(error.getMessage()).isEqualTo(
              "No characters found for user with ID [%s] and membership type [%s]".formatted(
                  membershipId, membershipType));
          assertThat(((NoCharactersFoundException) error).getErrorInteractionResponse()).isEqualTo(
              InteractionResponseData.builder()
                  .content(
                      "It seems that the user you were trying to find does not have any Destiny 2 characters")
                  .build());
        });
  }

  @Test
  @DisplayName("Get manifest entity is successful")
  void shouldGetManifestEntityCorrectly() {
    // given: manifest entity type and a hash
    ManifestEntity entity = ManifestEntity.ACTIVITY_DEFINITION;
    Long hash = 4897012345671L;

    BungieResponse<ManifestResponseFields> response = BungieResponse.of(
        ManifestResponseFields.builder()
            .hash(hash)
            .build());

    when(bungieClient.getManifestEntity(entity.getId(), hash))
        .thenReturn(Mono.just(response));

    // when: getManifestEntity is called
    // then: the manifest entity has the correct fields
    StepVerifier.create(sut.getManifestEntity(entity, hash))
        .assertNext(fields -> {
          assertThat(fields.getHash()).isEqualTo(hash);
        }).verifyComplete();
  }

  @Test
  @DisplayName("Get manifest throws an error when entity is empty")
  void shouldThrowExceptionIfManifestResponseIsEmpty() {
    // given: manifest entity type and a hash
    ManifestEntity entity = ManifestEntity.ACTIVITY_DEFINITION;
    Long hash = 4897012345671L;

    BungieResponse<ManifestResponseFields> response = BungieResponse.of(null);

    when(bungieClient.getManifestEntity(entity.getId(), hash))
        .thenReturn(Mono.just(response));

    // when: getManifestEntity is called
    // then: an exception is thrown with the correct error message
    StepVerifier.create(sut.getManifestEntity(entity, hash))
        .verifyErrorSatisfies(err -> {
          assertThat(err).isInstanceOf(ManifestEntityNotFoundException.class);
          assertThat(err.getMessage()).isEqualTo(
              "Manifest entity not found for [%s] and hash [%s]".formatted(entity, hash));
        });
  }

  @Test
  @DisplayName("Get public milestones is successful")
  void shouldGetPublicMilestones() {
    Map<String, MilestoneEntry> entries = Map.of(
        "milestone1",
        new MilestoneEntry(12491231L, ZonedDateTime.now().minusDays(4), ZonedDateTime.now(),
            Collections.emptyList())
    );
    BungieResponse<Map<String, MilestoneEntry>> response = BungieResponse.of(entries);
    when(bungieClient.getPublicMilestones())
        .thenReturn(Mono.just(response));

    // when: getPublicMilestones is invoked
    StepVerifier.create(sut.getPublicMilestones())
        .assertNext(milestones -> {
          assertThat(milestones).hasSize(1);
          assertThat(milestones).containsAllEntriesOf(entries);
        }).verifyComplete();
  }

  @Test
  @DisplayName("Get public milestones should throw an error when response is null")
  void shouldThrowExceptionOnEmptyResponse() {
    BungieResponse<Map<String, MilestoneEntry>> response = BungieResponse.of(null);
    when(bungieClient.getPublicMilestones()).thenReturn(Mono.just(response));

    // when: getPublicMilestones is invoked
    StepVerifier.create(sut.getPublicMilestones())
        .verifyErrorSatisfies(err -> {
          assertThat(err).isInstanceOf(ResourceNotFoundException.class);
          assertThat(err.getMessage()).isEqualTo(
              "No available milestone data was available for processing");
        });
  }

  @Test
  @DisplayName("retrieve players works successfully")
  void shouldRetrievePlayersSuccessfully() throws JsonProcessingException {
    // given: UserGlobalSearchBody and a page number
    UserGlobalSearchBody request = new UserGlobalSearchBody("deaht");

    List<UserSearchResult> userResults = List.of(
        new UserSearchResult("deaht", 6879, "13701231", Collections.emptyList()),
        new UserSearchResult("deaht", 7491, "13701231", Collections.emptyList()));
    BungieResponse<SearchResult> results = BungieResponse.of(
        new SearchResult(userResults, 0, false));
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(objectMapper.writeValueAsString(results)));

    // when: retrievePlayers is invoked
    // then: user search results are returned and have expected results
    StepVerifier.create(sut.retrievePlayers(request, 0))
        .assertNext(response -> {
          assertThat(response.getResponse().getSearchResults()).hasSize(2);
          assertThat(response.getResponse().getSearchResults()).containsAll(userResults);
          assertThat(response.getResponse().getHasMore()).isFalse();
          assertThat(response.getResponse().getPage()).isZero();
        });
  }

  @Test
  @DisplayName("Retrieve players should not throw exception on 5xx server responses")
  void shouldNotThrowExceptionOn5xxResponses() throws JsonProcessingException {
    // given: UserGlobalSearchBody and a page number
    UserGlobalSearchBody request = new UserGlobalSearchBody("deaht");

    BungieResponse<SearchResult> results = new BungieResponse<>(null, 217);
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(500)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(objectMapper.writeValueAsString(results)));

    // when: retrievePlayers is invoked
    // then: No exception is thrown
    StepVerifier.create(sut.retrievePlayers(request, 0))
        .assertNext(response -> {
          assertThat(response.getResponse()).isNull();
          assertThat(response.getErrorCode()).isEqualTo(results.getErrorCode());
        });
  }

  @Test
  @DisplayName("Retrieve players should not throw exception on 5xx server responses")
  void shouldThrowExceptionOnEveryOtherHttpStatusCode() throws JsonProcessingException {
    // given: UserGlobalSearchBody and a page number
    UserGlobalSearchBody request = new UserGlobalSearchBody("deaht");

    BungieResponse<SearchResult> results = new BungieResponse<>(null, 217);
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(401)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(objectMapper.writeValueAsString(results)));

    // when: retrievePlayers is invoked
    // then: A ResourceNotFound exception is thrown
    StepVerifier.create(sut.retrievePlayers(request, 0))
        .verifyErrorSatisfies(err -> {
          assertThat(err).isInstanceOf(ResourceNotFoundException.class);
          assertThat(err.getMessage()).isEqualTo(
              "Something wrong happened while retrieving users, user not found with value [%s]".formatted(
                  request));
        });
  }

}
