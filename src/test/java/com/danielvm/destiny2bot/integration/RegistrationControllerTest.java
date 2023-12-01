package com.danielvm.destiny2bot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import com.danielvm.destiny2bot.util.OAuth2Params;
import com.danielvm.destiny2bot.util.OAuth2Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class RegistrationControllerTest extends BaseIntegrationTest {

  @Autowired
  BungieConfiguration bungieConfiguration;

  @Autowired
  UserDetailsRepository userDetailsRepository;

  static final String SESSION_COOKIE = "SESSION";

  @Test
  @DisplayName("should save Discord user to httpSession after OAuth2 authorization")
  public void discordUserRegistration() throws Exception {

    // given: a login attempt from Discord user is received
    var authorizationCode = "userAuthorizationCode";
    var discordRequest = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    stubFor(post(urlPathEqualTo("/api/oauth2/token"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/discord-access-token.json")));

    stubFor(get(urlPathMatching("/api/v10/users/@me"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/user-@me-response.json")));

    stubFor(post(urlPathEqualTo("/platform/app/oauth/token/"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("bungie/bungie-access-token.json")));

    // when: the discord callback request is received
    var discordCallbackResponse = mockMvc.perform(discordRequest);

    // then: the response is a redirect URI to Bungie OAuth2 Authorization Flow
    discordCallbackResponse.andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(header().string(HttpHeaders.LOCATION,
            OAuth2Util.bungieAuthorizationUrl(bungieConfiguration)));

    var sessionCookie = discordCallbackResponse.andReturn().getResponse().getCookie(SESSION_COOKIE);

    // when: the bungie callback request is received
    var bungieRequest = MockMvcRequestBuilders.get("/bungie/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .cookie(sessionCookie)
        .accept(MediaType.APPLICATION_JSON);
    var bungieCallbackResponse = mockMvc.perform(bungieRequest);

    // then: bungie call back response returns status 204 NO_CONTENT
    bungieCallbackResponse.andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(status().isNoContent());

    // and: the user is saved in the database
    var discordUserFile = new ClassPathResource("__files/discord/user-@me-response.json");
    var discordUser = jsonMapper.readValue(discordUserFile.getFile(), DiscordUser.class);

    assertThat(userDetailsRepository.existsByDiscordId(discordUser.getId())).isTrue();
  }

  @Test
  @DisplayName("user registration fails when Discord returns a 4xx error")
  public void userRegistrationFailsWhenDiscordReturns4xxError() throws Exception {

    // given: a login attempt from Discord user is received
    var authorizationCode = "userAuthorizationCode";
    var discordRequest = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    var discordErrorJson = """
        {
            "message": "401: Unauthorized",
            "code": 0
        }
        """;

    stubFor(post(urlPathEqualTo("/api/oauth2/token"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.BAD_REQUEST.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(discordErrorJson)));

    // when: the discord callback request from a login attempt is received
    var response = mockMvc.perform(discordRequest);

    // then: the server returns a 4xx BAD_REQUEST status code
    response.andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value(discordErrorJson))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  @DisplayName("user registration fails when Discord returns a 5xx error")
  public void userRegistrationFailsWhenDiscordReturns5xxError() throws Exception {

    // given: a login attempt from Discord user is received
    var authorizationCode = "userAuthorizationCode";
    var discordRequest = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    var discordErrorJson = """
        {
            "message": "500: Something bad happened",
            "code": 0
        }
        """;

    stubFor(post(urlPathEqualTo("/api/oauth2/token"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(discordErrorJson)));

    // when: the discord callback request from a login attempt is received
    var response = mockMvc.perform(discordRequest);

    // then: the server returns a 5xx INTERNAL_SEVER status code
    response.andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value(discordErrorJson))
        .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  @DisplayName("user registration fails when Bungie returns a 4xx error")
  public void userRegistrationFailsWhenBungieReturns4xxError() throws Exception {

    // given: a login attempt from Discord user is received
    var authorizationCode = "userAuthorizationCode";
    var bungieErrorJson = """
        {
            "ErrorCode": 99,
            "ThrottleSeconds": 0,
            "ErrorStatus": "WebAuthRequired",
            "Message": "Please sign-in to continue.",
            "MessageData": {}
        }""";
    var discordRequest = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    stubFor(post(urlPathEqualTo("/api/oauth2/token"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/discord-access-token.json")));

    stubFor(get(urlPathMatching("/api/v10/users/@me"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/user-@me-response.json")));

    stubFor(post(urlPathEqualTo("/platform/app/oauth/token/"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.BAD_REQUEST.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(bungieErrorJson)));

    // when: the discord callback request is received
    var discordCallbackResponse = mockMvc.perform(discordRequest);

    // then: the response is a redirect URI to Bungie OAuth2 Authorization Flow
    discordCallbackResponse.andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(header().string(HttpHeaders.LOCATION,
            OAuth2Util.bungieAuthorizationUrl(bungieConfiguration)));

    var sessionCookie = discordCallbackResponse.andReturn().getResponse().getCookie(SESSION_COOKIE);

    // when: the bungie callback request is received from a user login
    var bungieRequest = MockMvcRequestBuilders.get("/bungie/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .cookie(sessionCookie)
        .accept(MediaType.APPLICATION_JSON);
    var bungieCallbackResponse = mockMvc.perform(bungieRequest);

    // then: bungie call back response returns status a 500 INTERNAL_SERVER error
    bungieCallbackResponse.andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value(bungieErrorJson))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  @DisplayName("user registration fails when Bungie returns a 5xx error")
  public void userRegistrationFailsWhenBungieReturns5xxError() throws Exception {

    // given: a login attempt from Discord user is received
    var authorizationCode = "userAuthorizationCode";
    var bungieErrorJson = """
        {
            "ErrorCode": 99,
            "ThrottleSeconds": 0,
            "ErrorStatus": "WebAuthRequired",
            "Message": "Please sign-in to continue.",
            "MessageData": {}
        }""";
    var discordRequest = MockMvcRequestBuilders.get("/discord/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .accept(MediaType.APPLICATION_JSON);

    stubFor(post(urlPathEqualTo("/api/oauth2/token"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/discord-access-token.json")));

    stubFor(get(urlPathMatching("/api/v10/users/@me"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile("discord/user-@me-response.json")));

    stubFor(post(urlPathEqualTo("/platform/app/oauth/token/"))
        .withHeader(HttpHeaders.CONTENT_TYPE,
            containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .withHeader(HttpHeaders.ACCEPT,
            containing(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(bungieErrorJson)));

    // when: the discord callback request is received
    var discordCallbackResponse = mockMvc.perform(discordRequest);

    // then: the response is a redirect URI to Bungie OAuth2 Authorization Flow
    discordCallbackResponse.andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(header().string(HttpHeaders.LOCATION,
            OAuth2Util.bungieAuthorizationUrl(bungieConfiguration)));

    var sessionCookie = discordCallbackResponse.andReturn().getResponse().getCookie(SESSION_COOKIE);

    // when: the bungie callback request is received from a user login
    var bungieRequest = MockMvcRequestBuilders.get("/bungie/callback")
        .queryParam(OAuth2Params.CODE, authorizationCode)
        .cookie(sessionCookie)
        .accept(MediaType.APPLICATION_JSON);
    var bungieCallbackResponse = mockMvc.perform(bungieRequest);

    // then: bungie call back response returns status a 500 INTERNAL_SERVER error
    bungieCallbackResponse.andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value(bungieErrorJson))
        .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }
}
