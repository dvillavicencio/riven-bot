package com.danielvm.destiny2bot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dao.UserDetailsReactiveDao;
import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.milestone.MilestoneEntry;
import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.Member;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import com.danielvm.destiny2bot.util.MessageUtil;
import com.danielvm.destiny2bot.util.OAuth2Util;
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
import java.util.Map;
import java.util.Objects;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;
import software.pando.crypto.nacl.Crypto;

public class InteractionControllerTest extends BaseIntegrationTest {

  private static final String VALID_PRIVATE_KEY = "F0EA3A0516695324C03ED552CD5A08A58CA1248172E8816C3BF235E52E75A7BF";
  private static final String MALICIOUS_PRIVATE_KEY = "CE4517095255B0C92D586AF9EEC27B998D68775363F9FE74341483FB3A657CEC";

  // Static mapper to be used on the @BeforeAll static method
  private static final ObjectMapper OBJECT_MAPPER = new JsonMapper.Builder(new JsonMapper())
      .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()
      .registerModule(new JavaTimeModule());

  @Autowired
  BungieConfiguration bungieConfiguration;
  @Autowired
  DiscordConfiguration discordConfiguration;
  @Autowired
  UserDetailsReactiveDao userDetailsReactiveDao;

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
    TypeReference<GenericResponse<Map<String, MilestoneEntry>>> typeReference = new TypeReference<>() {
    };
    var milestoneResponse = OBJECT_MAPPER.readValue(milestoneFile, typeReference);

    replaceDates(milestoneResponse, "526718853");
    replaceDates(milestoneResponse, "2712317338");

    OBJECT_MAPPER.writeValue(milestoneFile, milestoneResponse);
  }

  private static void replaceDates(GenericResponse<Map<String, MilestoneEntry>> response,
      String hash) {
    response.getResponse().entrySet().stream()
        .filter(entry -> Objects.equals(entry.getKey(), hash))
        .forEach(entry -> {
          var startDate = entry.getValue().getStartDate();
          var endDate = entry.getValue().getEndDate();
          if (Objects.nonNull(startDate)) {
            entry.getValue().setStartDate(MessageUtil.PREVIOUS_TUESDAY);
          }
          if (Objects.nonNull(endDate)) {
            entry.getValue().setEndDate(MessageUtil.NEXT_TUESDAY);
          }
        });
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
    Interaction body = Interaction.builder().id(1)
        .applicationId("theApplicationId").data(weeklyDungeonData).type(2)
        .build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

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
    var response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response JSON is correct
    response.expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.type").isEqualTo(4)
        .jsonPath("$.data.content").isEqualTo(
            """
                This week's dungeon is: Spire of the Watcher.
                You have until %s to complete it before the next dungeon in the rotation.
                """.formatted(MessageUtil.formatDate(MessageUtil.NEXT_TUESDAY.toLocalDate())));
  }

  @Test
  @DisplayName("get weekly raid works successfully")
  public void getWeeklyRaidWorksSuccessfully() throws JsonProcessingException, DecoderException {
    // given: a weekly_raid interaction with a valid signature
    InteractionData weeklyRaidData = InteractionData.builder()
        .id(2).name("weekly_raid").type(1)
        .build();
    Interaction body = Interaction.builder().id(1).applicationId("theApplicationId").type(2)
        .data(weeklyRaidData).build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

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
    var response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response JSON is correct
    response.expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.type").isEqualTo(4)
        .jsonPath("$.data.content").isEqualTo(
            """
                This week's raid is: Garden of Salvation.
                You have until %s to complete it before the next raid comes along.
                """.formatted(MessageUtil.formatDate(MessageUtil.NEXT_TUESDAY.toLocalDate())));
  }

  @Test
  @DisplayName("get weekly raid fails if no milestones are found")
  public void getWeeklyRaidsShouldThrowErrors() throws JsonProcessingException, DecoderException {
    // given: a weekly_raid interaction with a valid signature
    InteractionData weeklyRaidData = InteractionData.builder()
        .id(2).name("weekly_raid").type(1)
        .build();
    Interaction body = Interaction.builder().id(1).applicationId("theApplicationId").type(2)
        .data(weeklyRaidData).build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

    stubFor(get(urlPathEqualTo("/bungie/Destiny2/Milestones/"))
        .withHeader("x-api-key", equalTo(bungieConfiguration.getKey()))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/missing-api-key.json")));

    // when: the request is sent
    var response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response JSON is correct
    String errorJson;
    try {
      errorJson = objectMapper.writeValueAsString(
          objectMapper.readValue(
              new ClassPathResource("__files/bungie/missing-api-key.json").getInputStream(),
              Object.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    response.expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.detail").value(json -> assertJsonLenient(errorJson, json))
        .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Interactions fail if the signature is invalid")
  public void getWeeklyRaidInvalidSignature() throws JsonProcessingException, DecoderException {
    // given: an interaction with an invalid signature
    InteractionData data = InteractionData.builder()
        .id(2).name("weekly_raid").type(1).build();
    Interaction body = Interaction.builder()
        .id(1).applicationId("theApplicationId").type(2).data(data)
        .build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createInvalidSignature(body, timestamp);

    // when: the request is sent
    var response = webTestClient.post()
        .uri("/interactions")
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
        .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
        .jsonPath("$.detail").isEqualTo("interactions.request: Signature is invalid");
  }

  @Test
  @DisplayName("PING interactions with valid signatures are ack'd correctly")
  public void pingRequestsAreAckdCorrectly() throws JsonProcessingException, DecoderException {
    // given: an interaction with an invalid signature
    Interaction body = Interaction.builder().id(1).applicationId("theApplicationId").type(1)
        .build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

    // when: the request is sent
    var response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

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
    Interaction body = Interaction.builder().id(1).applicationId("theApplicationId").type(1)
        .build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createInvalidSignature(body, timestamp);

    // when: the request is sent
    var response = webTestClient.post()
        .uri("/interactions")
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
        .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
        .jsonPath("$.detail").isEqualTo("interactions.request: Signature is invalid");
  }

  @Test
  @DisplayName("Autocomplete requests for raid stats returns the user's characters successfully")
  public void autocompleteRequestsForRaidStats() throws DecoderException, JsonProcessingException {
    // given: a valid autocomplete request
    String username = "deahtstroke";
    String discordId = "123456";

    DiscordUser user = new DiscordUser(discordId, username);
    Member memberInfo = new Member(user);
    InteractionData data = InteractionData.builder().id("2").name("raid_stats").type(1)
        .build();
    Interaction body = Interaction.builder()
        .id("1").applicationId("theApplicationId").type(4).data(data).member(memberInfo)
        .build();
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

    // dummy entity in Redis
    String accessToken = "j7ondo?R0s9Ahff33DVt2M=CBCEsgtw30UAQGWnpQg1";
    String refreshToken = "V-pUflrJwOrG=bqQ/Ky3gJ-ioVg7b9/9xo?o-kFyHbZM9Zb";
    UserDetails entity = new UserDetails(discordId, username, accessToken, refreshToken,
        Instant.now().plusSeconds(9600));
    userDetailsReactiveDao.save(entity).subscribe();

    stubFor(get(urlPathEqualTo("/bungie/User/GetMembershipsForCurrentUser/"))
        .withHeader(HttpHeaders.AUTHORIZATION, equalTo(OAuth2Util.formatBearerToken(accessToken)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/bungie-membership-data.json")));

    stubFor(get(urlPathMatching("/bungie/Destiny2/3/Profile/0884665266181166124/"))
        .withQueryParam("components", equalTo("200"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/user-characters-response.json")));

    // when: the request is sent
    ResponseSpec response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response has the correct user character details and the 'All' option
    response
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.type").value(field -> assertThat(field).isEqualTo(8))
        .jsonPath("$.data.choices.size()").value(size -> assertThat(size).isEqualTo(4))
        .jsonPath("$.data.choices[?(@.value == 2305843009409260500)].name")
        .isEqualTo("[1810] Exo - Titan")
        .jsonPath("$.data.choices[?(@.value == 2305843009411211066)].name")
        .isEqualTo("[1626] Human - Hunter")
        .jsonPath("$.data.choices[?(@.value == 2305843010529424533)].name")
        .isEqualTo("[1777] Human - Warlock")
        .jsonPath("$.data.choices[?(@.name == 'All')].value")
        .isEqualTo("Gets stats for all characters");
  }

  @Test
  @DisplayName("Autocomplete requests for raid stats returns user characters when user only has one character")
  public void autocompleteRequestForUsersWithOneCharacter()
      throws DecoderException, JsonProcessingException {
    // given: a valid autocomplete request
    String username = "deahtstroke";
    String discordId = "123456";

    DiscordUser user = new DiscordUser(discordId, username);
    Member memberInfo = new Member(user);
    InteractionData data = InteractionData.builder()
        .id("2").name("raid_stats").type(1)
        .build();
    Interaction body = Interaction.builder()
        .id("1").applicationId("theApplicationId").type(4).data(data).member(memberInfo)
        .build();

    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

    // dummy entity in Redis
    String accessToken = "j7ondo?R0s9Ahff33DVt2M=CBCEsgtw30UAQGWnpQg1";
    String refreshToken = "V-pUflrJwOrG=bqQ/Ky3gJ-ioVg7b9/9xo?o-kFyHbZM9Zb";
    UserDetails entity = new UserDetails(discordId, username, accessToken, refreshToken,
        Instant.now().plusSeconds(9600));
    userDetailsReactiveDao.save(entity).subscribe();

    stubFor(get(urlPathEqualTo("/bungie/User/GetMembershipsForCurrentUser/"))
        .withHeader(HttpHeaders.AUTHORIZATION, equalTo(OAuth2Util.formatBearerToken(accessToken)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/bungie-membership-data.json")));

    stubFor(get(urlPathMatching("/bungie/Destiny2/3/Profile/0884665266181166124/"))
        .withQueryParam("components", equalTo("200"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/user-single-character-response.json")));

    // when: the request is sent
    ResponseSpec response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response has the correct user character details and the 'All' option is not present
    response
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.type").value(field -> assertThat(field).isEqualTo(8))
        .jsonPath("$.data.choices.size()").value(size -> assertThat(size).isEqualTo(1))
        .jsonPath("$.data.choices[?(@.value == 2305843009409260500)].name")
        .isEqualTo("[1810] Exo - Titan");
  }

  @Test
  @DisplayName("Autocomplete requests for raid stats should fail if the user does not have characters")
  public void autocompleteRequestFailsForEmptyCharactersFromBungie()
      throws DecoderException, JsonProcessingException {
    // given: a valid autocomplete request
    String username = "deahtstroke";
    String discordId = "123456";

    DiscordUser user = new DiscordUser(discordId, username);
    Member memberInfo = new Member(user);
    InteractionData data = InteractionData.builder()
        .id("1").name("raid_stats").type(1)
        .build();
    Interaction body = Interaction.builder()
        .id("1").applicationId("theApplicationId").data(data).member(memberInfo).type(4)
        .build();

    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = createValidSignature(body, timestamp);

    // dummy entity in Redis
    String accessToken = "j7ondo?R0s9Ahff33DVt2M=CBCEsgtw30UAQGWnpQg1";
    String refreshToken = "V-pUflrJwOrG=bqQ/Ky3gJ-ioVg7b9/9xo?o-kFyHbZM9Zb";
    UserDetails entity = new UserDetails(discordId, username, accessToken, refreshToken,
        Instant.now().plusSeconds(9600));
    userDetailsReactiveDao.save(entity).subscribe();

    stubFor(get(urlPathEqualTo("/bungie/User/GetMembershipsForCurrentUser/"))
        .withHeader(HttpHeaders.AUTHORIZATION, equalTo(OAuth2Util.formatBearerToken(accessToken)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/bungie-membership-data.json")));

    stubFor(get(urlPathMatching("/bungie/Destiny2/3/Profile/0884665266181166124/"))
        .withQueryParam("components", equalTo("200"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/empty-character-response.json")));

    // when: the request is sent
    ResponseSpec response = webTestClient.post()
        .uri("/interactions")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Signature-Ed25519", signature)
        .header("X-Signature-Timestamp", timestamp)
        .body(BodyInserters.fromValue(body))
        .exchange();

    // then: the response has the correct user character details and the 'All' option is not present
    response
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.detail").isEqualTo("No characters found for user [%s]".formatted(discordId))
        .jsonPath("$.status").isEqualTo(404);
  }
}