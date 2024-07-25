package com.deahtstroke.rivenbot.integration;

import com.deahtstroke.rivenbot.config.BungieConfiguration;
import com.deahtstroke.rivenbot.config.DiscordConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "spring.main.web-application-type=reactive")
@AutoConfigureWireMock(files = "/build/resources/test/__files")
@Testcontainers
public abstract class BaseIntegrationTest {

  @Container
  static final GenericContainer<?> redis = new GenericContainer<>(
      "redis:5.0.3-alpine").withExposedPorts(6379);

  @Autowired
  ReactiveWebApplicationContext reactiveWebApplicationContext;

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  DiscordConfiguration discordConfiguration;

  @Autowired
  BungieConfiguration bungieConfiguration;

  @DynamicPropertySource
  static void redisPropertySources(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  /**
   * Asserts a Json input is valid/invalid regardless of formatting, spacing, new-lines, etc.
   *
   * @param target   The target json to be compared
   * @param expected The expected json that the target should match
   */
  public static void assertJsonLenient(String target, Object expected) {
    try {
      JSONAssert.assertEquals(expected.toString(), target, false);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
