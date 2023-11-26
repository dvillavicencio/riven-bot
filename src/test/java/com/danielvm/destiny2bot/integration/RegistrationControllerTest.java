package com.danielvm.destiny2bot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.danielvm.destiny2bot.util.OAuth2Params;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class RegistrationControllerTest extends BaseIntegrationTest {

  @Test
  @DisplayName("should save Discord user to httpSession after OAuth2 authorization")
  public void discordUserRegistration() throws Exception {

    // given: an authorization code from Discord
    var discordAuthorizationCode = "someAuthorizationCode";
    var request = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, discordAuthorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    stubFor(post("/api/oauth2/token")
        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("discord-access-token.json")));

    stubFor(get("/users/@me")
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("discord-access-token.json")));

    // when: the callback request is received
    var response = mockMvc.perform(request);

    // then: the response is a redirect URI to Bungie OAuth2 Authorization Flow
    response.andDo(print())
        .andExpect(status().isOk());
  }

}
