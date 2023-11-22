package com.danielvm.destiny2bot.aop;

import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.context.UserIdentityContext;
import com.danielvm.destiny2bot.dto.oauth2.TokenResponse;
import com.danielvm.destiny2bot.entity.UserDetails;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import com.danielvm.destiny2bot.util.OAuth2Util;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Aspect
@Component
public class TokenValidationAspect {

    private final UserDetailsRepository userDetailsRepository;
    private final BungieConfiguration bungieConfiguration;
    private final WebClient.Builder webclient;

    public TokenValidationAspect(
            UserDetailsRepository userDetailsRepository,
            BungieConfiguration bungieConfiguration,
            WebClient.Builder webclient) {
        this.userDetailsRepository = userDetailsRepository;
        this.bungieConfiguration = bungieConfiguration;
        this.webclient = webclient;
    }

    /**
     * Advice that makes sure that before every method call that calls Bungie's API has
     * a valid access_token, if it does not, then refresh it.
     */
    @Before(value = "within(com.danielvm.destiny2bot..*) && " +
            "@annotation(com.danielvm.destiny2bot.annotation.Authorized)")
    public void accessTokenAdvice() {
        var discordUserId = UserIdentityContext.getUserIdentity().getDiscordId();
        var entity = userDetailsRepository.getUserDetailsByDiscordId(discordUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Database resource not found with user Id [%s]".formatted(discordUserId)));
        var isTokenExpired = Instant.now().isAfter(entity.getExpiration());
        if (isTokenExpired) {
            refreshToken(entity);
        }
    }

    private void refreshToken(UserDetails userDetails) {
        var tokenClient = webclient.baseUrl(bungieConfiguration.getTokenUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        MultiValueMap<String, String> bodyParams =
                OAuth2Util.buildRefreshTokenExchangeParameters(userDetails.getRefreshToken(),
                        bungieConfiguration.getClientId(), bungieConfiguration.getClientSecret());

        var tokenResponse = tokenClient.post().body(BodyInserters.fromFormData(bodyParams))
                .exchangeToMono(c -> {
                    if (c.statusCode().is2xxSuccessful()) {
                        return c.bodyToMono(TokenResponse.class);
                    } else {
                        return c.createException()
                                .flatMap(err -> Mono.error(new ResourceNotFoundException(err.getResponseBodyAsString())));
                    }
                }).block();
        Assert.notNull(tokenResponse, "The token response from Bungie was null");
        refreshDatabaseEntry(tokenResponse, userDetails);
    }

    private void refreshDatabaseEntry(TokenResponse tokenResponse, UserDetails oldEntity) {
        var newEntity = UserDetails.builder()
                .accessToken(tokenResponse.getAccessToken())
                .discordUsername(oldEntity.getDiscordUsername())
                .discordId(oldEntity.getDiscordId())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiration(Instant.now().plusSeconds(tokenResponse.getExpiresIn()))
                .build();
        userDetailsRepository.save(newEntity);
    }
}
