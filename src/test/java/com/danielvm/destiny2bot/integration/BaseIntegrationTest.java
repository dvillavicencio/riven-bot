package com.danielvm.destiny2bot.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  static JsonMapper jsonMapper;

  @Container
  static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(
      DockerImageName.parse("mongo"));
//      .withEnv(Map.of(
//          "MONGO_INITDB_ROOT_USERNAME", "test",
//          "MONGO_INITDB_ROOT_PASSWORD", "test",
//          "MONGO_INITDB_DATABASE", "destiny2bot-test"));

  @Container
  static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:5.0.3-alpine")
      .withExposedPorts(6379);

  @LocalServerPort
  protected int localServerPort;

  /**
   * Starts up some dynamic properties that change because of TestContainers usage, as well as the
   * test environment having a random port
   *
   * @param registry {@link DynamicPropertyRegistry}
   */
  @DynamicPropertySource
  public static void setupMe(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.port", MONGO_DB_CONTAINER::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", MONGO_DB_CONTAINER::getHost);
//    registry.add("spring.data.mongodb.username",
//        () -> MONGO_DB_CONTAINER.getEnvMap().get("MONGO_INITDB_ROOT_USERNAME"));
//    registry.add("spring.data.mongodb.password",
//        () -> MONGO_DB_CONTAINER.getEnvMap().get("MONGO_INITDB_ROOT_PASSWORD"));
//    registry.add("spring.data.mongodb.password",
//        () -> MONGO_DB_CONTAINER.getEnvMap().get("MONGO_INITDB_ROOT_PASSWORD"));

    registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
  }

  @BeforeAll
  public static void setup() {
    jsonMapper = new JsonMapper();
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

}
