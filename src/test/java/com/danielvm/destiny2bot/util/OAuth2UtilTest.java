package com.danielvm.destiny2bot.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OAuth2UtilTest {

  @Test
  @DisplayName("Format bearer token works successfully")
  public void formatBearerTokenWorksAsExpected() {
    // given: a token to format
    String token = "sometokentoformat";

    // when: formatBearerToken is called
    String bearerToken = OAuth2Util.formatBearerToken(token);

    // then: the returned bearer token is correct
    assertThat(bearerToken).isEqualTo("Bearer %s".formatted(token));
  }

  @Test
  @DisplayName("Build token exchange parameters works successfully")
  public void buildTokenExchangeParametersWorksSuccessfully() {
    // given: some OAuth2 parameters
    String authorizationCode = "someAuthorizationCode";
    String redirectUri = "someRedirectUri";
    String clientSecret = "someClientSecret";
    String clientId = "someClientId";

    // when: buildTokenExchangeParameters is called
    var responseMap = OAuth2Util.buildTokenExchangeParameters(authorizationCode, redirectUri,
        clientSecret, clientId);

    // then: the returned map contains all the values
    assertThatIterable(responseMap.get(OAuth2Params.CLIENT_ID)).contains(clientId);
    assertThatIterable(responseMap.get(OAuth2Params.REDIRECT_URI)).contains(redirectUri);
    assertThatIterable(responseMap.get(OAuth2Params.CLIENT_SECRET)).contains(clientSecret);
    assertThatIterable(responseMap.get(OAuth2Params.CODE)).contains(authorizationCode);
    assertThatIterable(responseMap.get(OAuth2Params.GRANT_TYPE)).contains(
        OAuth2Params.AUTHORIZATION_CODE);
  }

  @Test
  @DisplayName("Build token refresh parameters works successfully")
  public void buildTokenRefreshParametersWorksSuccessfully() {
    // given: some OAuth2 parameters
    String refreshToken = "someRefreshToken";
    String clientSecret = "someClientSecret";
    String clientId = "someClientId";

    // when: buildTokenExchangeParameters is called
    var responseMap = OAuth2Util.buildRefreshTokenExchangeParameters(refreshToken,
        clientId, clientSecret);

    // then: the returned map contains all the values
    assertThatIterable(responseMap.get(OAuth2Params.CLIENT_ID)).contains(clientId);
    assertThatIterable(responseMap.get(OAuth2Params.REFRESH_TOKEN)).contains(refreshToken);
    assertThatIterable(responseMap.get(OAuth2Params.CLIENT_SECRET)).contains(clientSecret);
    assertThatIterable(responseMap.get(OAuth2Params.GRANT_TYPE)).contains(
        OAuth2Params.REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Build bungie authorization URL is successful")
  public void bungieAuthorizationUrlWorksSuccessfully() {
    // given: some OAuth2 parameters
    String authUrl = "https://some.auth.url/oauth2/auth";
    String clientId = "someClientId";

    // when: buildTokenExchangeParameters is called
    var authorizationUrl = OAuth2Util.bungieAuthorizationUrl(authUrl, clientId);

    // then: the returned map contains all the values
    assertThat(authorizationUrl).isEqualTo("%s?response_type=code&client_id=%s"
        .formatted(authUrl, clientId));
  }

  @Test
  @DisplayName("Build discord callback URL is successful")
  public void discordAuthorizationUrlIsSuccessful() {
    // given: some OAuth2 parameters
    String authUrl = "https://some.auth.url/oauth2/auth";
    String clientId = "someClientId";
    String callbackUrl = "https://some.callback.url/discord/callback";
    String scopes = "identify";

    // when: discordAuthorizationUrl is called
    var authorizationUrl = OAuth2Util.discordAuthorizationUrl(authUrl, clientId,
        callbackUrl, scopes);

    // then: the returned map contains all the values
    assertThat(authorizationUrl).isEqualTo(
        "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s"
            .formatted(authUrl, clientId,
                URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8), scopes));
  }
}
