package com.deahtstroke.rivenbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.PostGameCarnageReport;
import com.deahtstroke.rivenbot.entity.PGCRDetails;
import com.deahtstroke.rivenbot.mapper.PGCRMapper;
import com.deahtstroke.rivenbot.repository.PGCRRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PGCRServiceTest {

  static MockWebServer mockWebServer;

  static ObjectMapper objectMapper = new JsonMapper();

  PGCRMapper mapper = Mappers.getMapper(PGCRMapper.class);

  @Mock
  PGCRRepository pgcrRepository;

  PGCRService sut;

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @BeforeAll
  public static void initialize() throws IOException {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @BeforeEach
  public void setup() {
    WebClient webClient = WebClient.builder()
        .baseUrl("http://localhost:" + mockWebServer.getPort())
        .build();
    sut = new PGCRService(webClient, mapper, pgcrRepository, objectMapper);
  }

  @Test
  @DisplayName("Retrieving a PGCR saves correct data when response is too big")
  void shouldSaveWhenFailedPGCR() {
    // given: the activity instanceID of a PGCR
    Long activityID = 123182391L;
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setChunkedBody(getJson("__files/bungie/big-pgcr.json"), 2048)
    );
    PGCRDetails mockPGCRDetails = new PGCRDetails(String.valueOf(activityID), false, null);
    when(pgcrRepository.existsById(activityID)).thenReturn(Mono.just(false));
    when(pgcrRepository.save(
        argThat(pgcr -> pgcr.getFromBeginning().equals(false) && pgcr.getInstanceId()
            .equals(String.valueOf(activityID))))).thenReturn(
        Mono.just(mockPGCRDetails));

    // when: getPGCR is called, no exception is thrown and the response is saved
    StepVerifier.create(sut.retrievePGCR(activityID))
        .assertNext(pgcrDetails -> {
          assertThat(pgcrDetails.getFromBeginning()).isFalse();
          assertThat(pgcrDetails.getInstanceId()).isEqualTo(String.valueOf(activityID));
        }).verifyComplete();
  }

  @Test
  @DisplayName("Retrieving a PGCR saves correct data when response is less than limit")
  void shouldSaveWhenSuccessfulPGCR() throws JsonProcessingException {
    // given: the activity instanceID of a PGCR
    Long activityId = 8231900504L;
    String filePath = "__files/bungie/small-pgcr.json";
    String jsonContent = getJson(filePath);
    mockWebServer.enqueue(
        new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setChunkedBody(jsonContent, 2048)
    );

    var bungieResponse = new TypeReference<BungieResponse<PostGameCarnageReport>>() {
    };
    var details = objectMapper.readValue(jsonContent, bungieResponse);
    var pgcrDetails = mapper.dtoToEntity(details.getResponse(), activityId);
    var pgcrPlayerEntry = pgcrDetails.getPlayers().stream()
        .filter(ped -> ped.getPlayerName().equals("Deaht")).findFirst().orElse(null);
    when(pgcrRepository.existsById(activityId)).thenReturn(Mono.just(false));
    when(pgcrRepository.save(argThat(pgcr ->
        pgcr.getInstanceId().equals(pgcrDetails.getInstanceId()))
    )).thenReturn(Mono.just(pgcrDetails));

    // when: retrieve PGCR is called, the actual PGCR is valid and non-empty
    StepVerifier.create(sut.retrievePGCR(activityId))
        .assertNext(pgcr -> {
          assertThat(pgcr.getInstanceId()).isEqualTo(String.valueOf(activityId));
          assertThat(pgcr.getFromBeginning()).isFalse();
          assertThat(pgcr.getPlayers()).hasSize(6);
          assertThat(pgcr.getPlayers()).contains(pgcrPlayerEntry);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Retrieving a PGCR returns correct data if it is found in the DB")
  void shouldRetrievePGCRFromDB() throws JsonProcessingException {
    // given: instanceID of a PGCR
    var activityId = 8231900504L;
    String filePath = "__files/bungie/small-pgcr.json";
    var bungieResponse = new TypeReference<BungieResponse<PostGameCarnageReport>>() {
    };
    var details = objectMapper.readValue(getJson(filePath), bungieResponse);
    var pgcrDetails = mapper.dtoToEntity(details.getResponse(), activityId);

    when(pgcrRepository.existsById(activityId)).thenReturn(Mono.just(true));
    when(pgcrRepository.findById(activityId)).thenReturn(Mono.just(pgcrDetails));

    // when: retrievePGCR is called
    // then: the database is called to retrieve a PGCR instead
    StepVerifier.create(sut.retrievePGCR(activityId))
        .assertNext(pgcr -> {
          assertThat(pgcr.getInstanceId()).isEqualTo(pgcrDetails.getInstanceId());
          assertThat(pgcr.getFromBeginning()).isEqualTo(pgcrDetails.getFromBeginning());
          assertThat(pgcr.getPlayers()).containsAll(pgcrDetails.getPlayers());
        })
        .verifyComplete();
  }

  private String getJson(String path) {
    try {
      InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream(path);
      assert jsonStream != null;
      return new String(jsonStream.readAllBytes());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
