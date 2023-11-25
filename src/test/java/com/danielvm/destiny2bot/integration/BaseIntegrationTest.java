package com.danielvm.destiny2bot.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 6969)
public abstract class BaseIntegrationTest {

  @Container
  static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
      DockerImageName.parse("mongo"));

  @Container
  static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:5.0.3-alpine")
      .withExposedPorts(6379);

  @LocalServerPort
  protected static int localServerPort;

  @DynamicPropertySource
  public static void setupMe(DynamicPropertyRegistry registry) {
    Startables.deepStart(mongoDBContainer, redisContainer).join();

    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);

    registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    registry.add("spring.data.redis.host", redisContainer::getHost);

    registry.add("application.callback.url",
        () -> "http://localhost:%s".formatted(localServerPort));
  }

}
