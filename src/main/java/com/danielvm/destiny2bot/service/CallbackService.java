package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.DiscordClient;
import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.oauth2.TokenResponse;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CallbackService {

    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String DISCORD_USER_ID_KEY = "discordUserId";
    private static final String DISCORD_USER_ALIAS_KEY = "discordUserAlias";

    private final DiscordConfiguration discordConfiguration;
    private final BungieConfiguration bungieConfiguration;
    private final WebClient.Builder webClient;
    private final DiscordClient discordClient;
    private final UserDetailsRepository userDetailsRepository;


    public CallbackService(
            DiscordConfiguration discordConfiguration,
            BungieConfiguration bungieConfiguration,
            WebClient.Builder webClientBuilder,
            DiscordClient discordClient, UserDetailsRepository userDetailsRepository) {
        this.discordConfiguration = discordConfiguration;
        this.bungieConfiguration = bungieConfiguration;
        this.webClient = webClientBuilder;
        this.discordClient = discordClient;
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * Retrieve DiscordUserId from authenticated user and save it to Session
     *
     * @param authorizationCode The authorization code from Discord
     * @param session           The HttpSession the user is linked to
     */
    public void authenticateDiscordUser(String authorizationCode, HttpSession session) {
        TokenResponse tokenResponse = getTokenResponse(authorizationCode, discordConfiguration.getCallbackUrl(),
                discordConfiguration.getClientSecret(), discordConfiguration.getClientId(), discordConfiguration.getTokenUrl());

        Assert.notNull(tokenResponse, "The token response was null");
        Assert.notNull(tokenResponse.getAccessToken(), "The access_token received is null");

        var user = discordClient.getUser(
                AuthenticationUtil.formatBearerToken(tokenResponse.getAccessToken())).getBody();

        Assert.notNull(user, "User response is null");
        Assert.notNull(user.getId(), "User Id is null");
        Assert.notNull(user.getUsername(), "User Alias is null");

        session.setAttribute(DISCORD_USER_ID_KEY, user.getId());
        session.setAttribute(DISCORD_USER_ALIAS_KEY, user.getUsername());
    }

    /**
     * Link the Bungie credentials to the current DiscordUserId and create a database entry
     *
     * @param authorizationCode The authorization code from Bungie
     * @param httpSession       The HttpSession the user is linked to
     */
    public void registerUser(String authorizationCode, HttpSession httpSession) {
        var tokenResponse = getTokenResponse(authorizationCode, bungieConfiguration.getCallbackUrl(),
                bungieConfiguration.getClientSecret(), bungieConfiguration.getClientId(), bungieConfiguration.getTokenUrl());
        Assert.notNull(tokenResponse, "The token response from Bungie is null");

        UserDetails userDetails = UserDetails.builder()
                .discordUsername((String) httpSession.getAttribute(DISCORD_USER_ALIAS_KEY))
                .discordId((String) httpSession.getAttribute(DISCORD_USER_ID_KEY))
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .build();
        log.info("Saving user [{}] to MongoDB", userDetails);
        userDetailsRepository.save(userDetails);
        httpSession.invalidate();
    }

    private TokenResponse getTokenResponse(
            String authorizationCode, String callbackUrl, String clientSecret, String clientId, String tokenUrl) {
        MultiValueMap<String, String> map = buildFormParameters(
                authorizationCode,
                callbackUrl,
                clientSecret,
                clientId);

        var client = webClient.baseUrl(tokenUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        return client.post().body(BodyInserters.fromFormData(map))
                .exchangeToMono(c -> c.bodyToMono(TokenResponse.class))
                .block();
    }

    private MultiValueMap<String, String> buildFormParameters(
            String authorizationCode, String redirectUri, String clientSecret, String clientId) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", authorizationCode);
        map.add("grant_type", AUTHORIZATION_CODE);
        map.add("redirect_uri", redirectUri);
        map.add("client_secret", clientSecret);
        map.add("client_id", clientId);
        return map;
    }
}
