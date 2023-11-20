package com.danielvm.destiny2bot.aop;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.discord.interactions.DiscordUser;
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
    private final DiscordConfiguration discordConfiguration;
    private final WebClient.Builder webclient;

    public TokenValidationAspect(
            UserDetailsRepository userDetailsRepository,
            DiscordConfiguration discordConfiguration,
            WebClient.Builder webclient) {
        this.userDetailsRepository = userDetailsRepository;
        this.discordConfiguration = discordConfiguration;
        this.webclient = webclient;
    }

    /**
     * Advice that makes sure that before every method call that involves a Discord
     * @param discordUser
     */
    @Before(value = "@target(com.danielvm.destiny2bot.annotation.Authorized) " +
            "&& args(discordUser)")
    public void accessTokenAdvice(DiscordUser discordUser) {
        var entity = userDetailsRepository.getUserDetailsByDiscordId(discordUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Database resource not found with user Id [%s]".formatted(discordUser.getId())));
        var isTokenExpired = Instant.now().isAfter(entity.getExpiration());
        if (isTokenExpired) {
            refreshToken(entity);
        }
    }

    private void refreshToken(UserDetails userDetails) {
        var tokenClient = webclient.baseUrl(discordConfiguration.getTokenUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        MultiValueMap<String, String> bodyParams =
                OAuth2Util.buildRefreshTokenExchangeParameters(userDetails.getRefreshToken());

        var tokenResponse = tokenClient.post().body(BodyInserters.fromFormData(bodyParams))
                .exchangeToMono(c -> {
                    if (c.statusCode().is2xxSuccessful()) {
                        return c.bodyToMono(TokenResponse.class);
                    } else {
                        return c.createException()
                                .flatMap(err -> Mono.error(new ResourceNotFoundException(err.getResponseBodyAsString())));
                    }
                }).block();
        Assert.notNull(tokenResponse, "The token response from Discord was null");
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
