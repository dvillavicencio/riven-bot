package com.danielvm.destiny2bot.service;

import static java.util.Objects.isNull;

import com.danielvm.destiny2bot.client.DiscordClient;
import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.oauth2.TokenResponse;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import com.danielvm.destiny2bot.util.OAuth2Util;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserAuthorizationService {

  private static final String DISCORD_USER_ID_KEY = "discordUserId";
  private static final String DISCORD_USER_ALIAS_KEY = "discordUserAlias";

  private final DiscordConfiguration discordConfiguration;
  private final BungieConfiguration bungieConfiguration;
  private final DiscordClient discordClient;
  private final UserDetailsRepository userDetailsRepository;
  private final WebClient.Builder defaultWebClientBuilder;


  public UserAuthorizationService(
      DiscordConfiguration discordConfiguration,
      BungieConfiguration bungieConfiguration,
      DiscordClient discordClient,
      UserDetailsRepository userDetailsRepository,
      Builder defaultWebClientBuilder) {
    this.discordConfiguration = discordConfiguration;
    this.bungieConfiguration = bungieConfiguration;
    this.discordClient = discordClient;
    this.userDetailsRepository = userDetailsRepository;
    this.defaultWebClientBuilder = defaultWebClientBuilder;
  }

  /**
   * Retrieve DiscordUserId from authenticated user and save it to Session
   *
   * @param authorizationCode The authorization code from Discord
   * @param session           The HttpSession the user is linked to
   */
  public void authenticateDiscordUser(String authorizationCode, HttpSession session) {
    TokenResponse tokenResponse = getTokenResponse(authorizationCode,
        discordConfiguration.getCallbackUrl(), discordConfiguration.getClientSecret(),
        discordConfiguration.getClientId(), discordConfiguration.getTokenUrl());

    Assert.notNull(tokenResponse.getAccessToken(), "The access_token received is null");

    var user = discordClient.getUser(
        OAuth2Util.formatBearerToken(tokenResponse.getAccessToken())).getBody();

    if (isNull(user) || isNull(user.getId()) || isNull(
        user.getUsername())) {
      log.error("The user object [{}] is null or has null required attributes", user);
      throw new IllegalStateException(
          "Some required arguments for registration are null for the current user");
    }

    session.setAttribute(DISCORD_USER_ID_KEY, user.getId());
    session.setAttribute(DISCORD_USER_ALIAS_KEY, user.getUsername());
  }

  /**
   * Link the Bungie credentials to the current DiscordUserId and create a database entry
   *
   * @param authorizationCode The authorization code from Bungie
   * @param httpSession       The HttpSession the user is linked to
   */
  public void linkDiscordUserToBungieAccount(String authorizationCode, HttpSession httpSession) {
    var tokenResponse = getTokenResponse(authorizationCode, bungieConfiguration.getCallbackUrl(),
        bungieConfiguration.getClientSecret(), bungieConfiguration.getClientId(),
        bungieConfiguration.getTokenUrl());

    List<Object> unvalidatedFields = new ArrayList<>();
    unvalidatedFields.add(tokenResponse.getAccessToken());
    unvalidatedFields.add(tokenResponse.getRefreshToken());
    unvalidatedFields.add(tokenResponse.getExpiresIn());

    Assert.noNullElements(unvalidatedFields,
        "Some required fields from Bungie's access_token are null, unable to register current user");

    UserDetails userDetails = UserDetails.builder()
        .discordUsername((String) httpSession.getAttribute(DISCORD_USER_ALIAS_KEY))
        .discordId((String) httpSession.getAttribute(DISCORD_USER_ID_KEY))
        .accessToken(tokenResponse.getAccessToken())
        .refreshToken(tokenResponse.getRefreshToken())
        .expiration(Instant.now().plusSeconds(tokenResponse.getExpiresIn()))
        .build();
    userDetailsRepository.save(userDetails);
    httpSession.invalidate();
  }

  private TokenResponse getTokenResponse(
      String authorizationCode, String callbackUrl, String clientSecret, String clientId,
      String tokenUrl) {
    MultiValueMap<String, String> map =
        OAuth2Util.buildTokenExchangeParameters(authorizationCode, callbackUrl, clientSecret,
            clientId);

    var tokenClient = defaultWebClientBuilder
        .baseUrl(tokenUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();

    return tokenClient.post().body(BodyInserters.fromFormData(map))
        .retrieve()
        .bodyToMono(TokenResponse.class)
        .switchIfEmpty(Mono.just(new TokenResponse())) // fallback to empty token response
        .block();
  }

}
