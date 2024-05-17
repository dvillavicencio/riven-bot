package com.deahtstroke.rivenbot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.milestone.MilestoneEntry;
import com.deahtstroke.rivenbot.dto.discord.Choice;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionData;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.enums.InteractionType;
import com.deahtstroke.rivenbot.enums.ManifestEntity;
import com.deahtstroke.rivenbot.enums.Raid;
import com.deahtstroke.rivenbot.enums.RaidEncounter;
import com.deahtstroke.rivenbot.util.MessageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;
import software.pando.crypto.nacl.Crypto;

public class InteractionControllerTest extends BaseIntegrationTest {

  private static final String MALICIOUS_PRIVATE_KEY = "CE4517095255B0C92D586AF9EEC27B998D68775363F9FE74341483FB3A657CEC";
  private static final String VALID_PRIVATE_KEY = "F0EA3A0516695324C03ED552CD5A08A58CA1248172E8816C3BF235E52E75A7BF";

  // Static mapper to be used on the @BeforeAll static method
  private static final ObjectMapper OBJECT_MAPPER = new JsonMapper.Builder(new JsonMapper())
      .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()
      .registerModule(new JavaTimeModule());

  /**
   * This method replaces all the placeholder values in the milestones-response.json file The reason
   * for this is that the /weekly_raid and /weekly_dungeon responses will be weird if the dates are
   * not dynamic, therefore this method
   *
   * @throws IOException in case we are not able to write back to the file (in-place)
   */
  @BeforeAll
  public static void before() throws IOException {
    File milestoneFile = new File("src/test/resources/__files/bungie/milestone-response.json");
    TypeReference<BungieResponse<Map<String, MilestoneEntry>>> typeReference = new TypeReference<>() {
    };
    var milestoneResponse = OBJECT_MAPPER.readValue(milestoneFile, typeReference);

    replaceDates(milestoneResponse, "526718853");
    replaceDates(milestoneResponse, "2712317338");

    OBJECT_MAPPER.writeValue(milestoneFile, milestoneResponse);
  }

  private static void replaceDates(BungieResponse<Map<String, MilestoneEntry>> response,
      String hash) {
    response.getResponse().entrySet().stream()
        .filter(entry -> Objects.equals(entry.getKey(), hash))
        .forEach(entry -> {
          var startDate = entry.getValue().getStartDate();
          var endDate = entry.getValue().getEndDate();
          if (Objects.nonNull(startDate)) {
            entry.getValue().setStartDate(MessageUtils.PREVIOUS_TUESDAY);
          }
          if (Objects.nonNull(endDate)) {
            entry.getValue().setEndDate(MessageUtils.NEXT_TUESDAY);
          }
        });
  }

  /**
   * Sends a partial WebTestClient request to the specified route with a valid signature
   *
   * @param endpoint    The /endpoint to call
   * @param interaction The interaction body that the user will send along this request
   * @return {@link WebTestClient.RequestHeadersSpec}
   * @throws DecoderException        If something goes wrong with the Crypto library
   * @throws JsonProcessingException If Jackson is unable to parse the body to Json for signing
   */
  public ResponseSpec sendValidSignatureRequest(String endpoint, Interaction interaction)
      throws DecoderException, JsonProcessingException {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(interaction, timestamp);
    return this.webTestClient.post().uri(endpoint)
        .accept(MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(interaction))
        .exchange();
  }

  /**
   * Sends a WebTestClient request with the appropriate headers but with an invalid signature
   *
   * @param endpoint    The /endpoint to call
   * @param interaction The interaction body that the user will send along this request
   * @return {@link WebTestClient.ResponseSpec}
   * @throws DecoderException        If something goes wrong with the Crypto library
   * @throws JsonProcessingException If Jackson is unable to parse the body to Json for signing
   */
  public ResponseSpec sendInvalidSignatureRequest(String endpoint, Interaction interaction)
      throws DecoderException, JsonProcessingException {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createInvalidSignature(interaction, timestamp);
    return this.webTestClient.post().uri(endpoint)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(interaction))
        .exchange();
  }

  private String createValidSignature(Interaction body, String timestamp)
      throws JsonProcessingException, DecoderException {
    KeyPair signingKeys = Crypto.seedSigningKeyPair(Hex.decodeHex(VALID_PRIVATE_KEY.toCharArray()));
    discordConfiguration.setBotPublicKey(
        Hex.encodeHexString(
            signingKeys.getPublic().getEncoded())); // change the public key in the config class

    var signatureBytes = Crypto.sign(signingKeys.getPrivate(),
        (timestamp + objectMapper.writeValueAsString(body)).getBytes(StandardCharsets.UTF_8));
    return Hex.encodeHexString(signatureBytes);
  }

  private String createInvalidSignature(Interaction body, String timestamp)
      throws JsonProcessingException, DecoderException {
    KeyPair invalidSigningKeyPair = Crypto.seedSigningKeyPair(
        Hex.decodeHex(MALICIOUS_PRIVATE_KEY.toCharArray()));

    PublicKey validPublicKey = Crypto.seedSigningKeyPair(
        Hex.decodeHex(VALID_PRIVATE_KEY.toCharArray())).getPublic();

    discordConfiguration.setBotPublicKey(
        Hex.encodeHexString(validPublicKey.getEncoded()));

    var signatureBytes = Crypto.sign(invalidSigningKeyPair.getPrivate(),
        (timestamp + objectMapper.writeValueAsString(body)).getBytes(StandardCharsets.UTF_8));
    return Hex.encodeHexString(signatureBytes);
  }

  @Test
  @DisplayName("get weekly dungeon works successfully")
  public void getWeeklyDungeonWorksSuccessfully() throws JsonProcessingException, DecoderException {
    // given: a weekly_dungeon interaction with a valid signature
    InteractionData weeklyDungeonData = InteractionData.builder()
        .id(2).name("weekly_dungeon").type(1)
        .build();
    Interaction body = Interaction.builder().id(1L)
        .applicationId("theApplicationId").data(weeklyDungeonData).type(2)
        .build();

    stubFor(get(urlPathEqualTo("/bungie/Destiny2/Milestones/"))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/milestone-response.json")));

    var activityDefinition = ManifestEntity.ACTIVITY_DEFINITION.getId();
    var activityHash = "1262462921";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(activityDefinition, activityHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/dungeon-activity-response.json")));

    var masterDungeonHash = "2296818662";
    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(activityDefinition, masterDungeonHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/master-dungeon-activity-response.json")));

    var activityTypeDefinition = ManifestEntity.ACTIVITY_TYPE_DEFINITION.getId();
    var activityTypeHash = "608898761";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(activityTypeDefinition, activityTypeHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/dungeon-activity-type-response.json")));

    var milestoneDefinition = ManifestEntity.MILESTONE_DEFINITION.getId();
    var milestoneHash = "526718853";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(milestoneDefinition, milestoneHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/dungeon-milestone-response.json")));

    // when: the request is sent
    var response = sendValidSignatureRequest("/interactions", body);

    // then: the response JSON is correct
    response.expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.type").isEqualTo(4)
        .jsonPath("$.data.content").isEqualTo(
            """
                This week's dungeon is: Spire of the Watcher.
                You have until %s to complete it before the next dungeon in the rotation.
                """.formatted(MessageUtils.formatDate(MessageUtils.NEXT_TUESDAY.toLocalDate())));
  }

  @Test
  @DisplayName("get weekly raid works successfully")
  public void getWeeklyRaidWorksSuccessfully() throws JsonProcessingException, DecoderException {
    // given: a weekly_raid interaction with a valid signature
    InteractionData weeklyRaidData = InteractionData.builder()
        .id(2).name("weekly_raid").type(1)
        .build();
    Interaction body = Interaction.builder().id(1L).applicationId("theApplicationId").type(2)
        .data(weeklyRaidData).build();

    stubFor(get(urlPathEqualTo("/bungie/Destiny2/Milestones/"))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/milestone-response.json")));

    var activityDefinition = ManifestEntity.ACTIVITY_DEFINITION.getId();
    var activityHash = "1042180643";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(activityDefinition, activityHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/raid-activity-response.json")));

    var activityTypeDefinition = ManifestEntity.ACTIVITY_TYPE_DEFINITION.getId();
    var activityTypeHash = "2043403989";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(activityTypeDefinition, activityTypeHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/raid-activity-type-response.json")));

    var milestoneDefinition = ManifestEntity.MILESTONE_DEFINITION.getId();
    var milestoneHash = "2712317338";

    stubFor(get(urlPathEqualTo(
        "/bungie/Destiny2/Manifest/%s/%s/".formatted(milestoneDefinition, milestoneHash)))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/raid-milestone-response.json")));

    // when: the request is sent
    var response = sendValidSignatureRequest("/interactions", body);

    // then: the response JSON is correct
    response.expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.type").isEqualTo(4)
        .jsonPath("$.data.content").isEqualTo(
            """
                This week's raid is: Garden of Salvation.
                You have until %s to complete it before the next raid comes along.
                """.formatted(MessageUtils.formatDate(MessageUtils.NEXT_TUESDAY.toLocalDate())));
  }

  @Test
  @DisplayName("get weekly raid fails if no milestones are found")
  public void getWeeklyRaidsShouldThrowErrors() throws JsonProcessingException, DecoderException {
    // given: a weekly_raid interaction with a valid signature
    InteractionData weeklyRaidData = InteractionData.builder()
        .id(2).name("weekly_raid").type(1)
        .build();
    Interaction body = Interaction.builder().id(1L).applicationId("theApplicationId").type(2)
        .data(weeklyRaidData).build();

    stubFor(get(urlPathEqualTo("/bungie/Destiny2/Milestones/"))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/no-weekly-data.json")));

    // when: the request is sent
    var response = sendValidSignatureRequest("/interactions", body);

    // then: the response JSON is correct
    response.expectStatus()
        .is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo(500)
        .jsonPath("$.detail").isEqualTo("No available milestone data was available for processing")
        .returnResult();
  }

  @Test
  @DisplayName("Interactions fail if the signature is invalid")
  public void invalidSignatureInteraction() throws JsonProcessingException, DecoderException {
    // given: an interaction with an invalid signature
    InteractionData data = InteractionData.builder()
        .id(2)
        .name("weekly_raid")
        .type(1).build();
    Interaction body = Interaction.builder()
        .id(1L)
        .applicationId("theApplicationId")
        .type(2)
        .data(data).build();

    // when: the request is sent
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createInvalidSignature(body, timestamp);
    var response = webTestClient.post().uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response JSON has the correct error message
    response.expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo(400)
        .jsonPath("$.detail")
        .isEqualTo("The signature passed in was invalid. Timestamp: [%s], Signature [%s]"
            .formatted(timestamp, signature));
  }

  @Test
  @DisplayName("PING interactions with valid signatures are ack'd correctly")
  public void pingRequestsAreAckdCorrectly() throws JsonProcessingException, DecoderException {
    // given: an interaction with an invalid signature
    Interaction body = Interaction.builder().id(1L).applicationId("theApplicationId").type(1)
        .build();

    // when: the request is sent
    var response = sendValidSignatureRequest("/interactions", body);

    // then: the response JSON has the correct error message
    response.expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.type").isEqualTo(InteractionType.PING.getType());
  }

  @Test
  @DisplayName("PING interactions with invalid signatures are not ack'd")
  public void invalidPingRequestsAreNotAckd() throws JsonProcessingException, DecoderException {
    // given: an interaction with an invalid signature
    Interaction body = Interaction.builder().id(1L)
        .applicationId("theApplicationId").type(1)
        .build();

    // when: the request is sent
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createInvalidSignature(body, timestamp);
    var response = webTestClient.post().uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response JSON has the correct error message
    response.expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo(400)
        .jsonPath("$.detail")
        .isEqualTo("The signature passed in was invalid. Timestamp: [%s], Signature [%s]".formatted(
            timestamp, signature));
  }

  @Test
  @DisplayName("Autocomplete requests for a raid map request is successful")
  public void autocompleteRequestsForRaidMapAreSuccessful()
      throws DecoderException, IOException {
    // given: an interaction for autocomplete for the /raid_map command
    List<Option> options = List.of(
        new Option("raid", 3, "last_wish", false, Collections.emptyList()),
        new Option("raid", 3, "k", true, Collections.emptyList())
    );
    InteractionData data = InteractionData.builder()
        .id("someID")
        .name("raid_map")
        .options(options).build();
    Interaction body = Interaction.builder()
        .id(1L)
        .data(data)
        .type(InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE.getType())
        .build();

    // when: the raid_map autocomplete interaction is requested
    var response = sendValidSignatureRequest("/interactions", body);

    // then: the correct raid encounters options are returned
    List<Choice> expectedChoices = RaidEncounter.getRaidEncounters(Raid.LAST_WISH)
        .map(raidEncounter -> new Choice(raidEncounter.getName(), raidEncounter.getDirectory()))
        .toStream().toList();

    byte[] responseBody = response.expectStatus().is2xxSuccessful()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.type").isEqualTo(8)
        .jsonPath("$.data.choices.size()").isEqualTo(5)
        .jsonPath("$.data.choices").isArray()
        .returnResult().getResponseBody();

    InteractionResponse interactionResponse = objectMapper.readValue(responseBody,
        InteractionResponse.class);

    assertThat(interactionResponse.getData().getChoices()).containsAll(expectedChoices);
  }

  @Test
  @DisplayName("Command request for raid map request is for 1 image")
  public void commandRequestsForRaidMapAreSuccessful()
      throws DecoderException, IOException {
    // given: an interaction for autocomplete for the /raid_map command
    List<Option> options = List.of(
        new Option("raid", 3, "last_wish", false, Collections.emptyList()),
        new Option("encounter", 3, "kalli", false, Collections.emptyList())
    );
    InteractionData data = InteractionData.builder()
        .id("someID")
        .name("raid_map")
        .options(options).build();
    Interaction body = Interaction.builder()
        .id(1L)
        .data(data)
        .type(InteractionType.APPLICATION_COMMAND.getType())
        .build();

    // when: the raid_map autocomplete interaction is requested
    var response = sendValidSignatureRequest("/interactions", body);

    // TODO: Revisit this integration test for assertions on the multipart parts
    // then: the correct raid encounters options are returned
    response.expectStatus().is2xxSuccessful()
        .expectHeader().value("Content-Type", containsString("multipart/form-data"));

  }

}