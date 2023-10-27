package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieApiConfig;
import com.danielvm.destiny2bot.dto.destiny.membership.DestinyMembershipResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

@Component
@Slf4j
public class MembershipClient extends BungieClient {

    private final RestTemplate restTemplate;
    private final BungieApiConfig bungieApiConfig;

    public MembershipClient(
            RestTemplate restTemplate,
            BungieApiConfig bungieApiConfig) {
        this.restTemplate = restTemplate;
        this.bungieApiConfig = bungieApiConfig;
    }

    /**
     * Gets Bungie membership data for the currently logged-in user
     *
     * @return {@link DestinyMembershipResponse}
     * @throws Exception exception
     */
    public DestinyMembershipResponse getMembershipDataForCurrentUser() throws Exception {
        var accessToken = Objects.requireNonNull(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal().getAttribute("access_token"));
        HttpHeaders headers = buildAuthorizedHttpRequestHeaders(bungieApiConfig.getKey(), Objects.requireNonNull(accessToken).toString());
        var uri = URI.create(bungieApiConfig.getCurrentUserMembershipUrl());
        var httpEntity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, DestinyMembershipResponse.class).getBody();
        } catch (RestClientResponseException rce) {
            throw new Exception("Something went wrong: %s".formatted(rce.getResponseBodyAs(String.class)), rce.getCause());
        }
    }
}
