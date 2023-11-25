package com.danielvm.destiny2bot.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class RegistrationControllerTest extends BaseIntegrationTest {

  @Test
  @DisplayName("should save Discord user to httpSession after OAuth2 authorization")
  public void discordUserRegistration() {

  }

}
