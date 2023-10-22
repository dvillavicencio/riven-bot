package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieApiConfig;
import com.danielvm.destiny2bot.dto.destinydomain.membership.DestinyMembershipResponse;
import com.danielvm.destiny2bot.dto.destinydomain.profile.DestinyProfileResponse;
import com.danielvm.destiny2bot.enums.ComponentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BungieApiClient {

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String BEARER_TOKEN_FORMAT = "Bearer %s";
    private final RestTemplate restTemplate;
    private final BungieApiConfig bungieApiConfig;

    public BungieApiClient(
            RestTemplate restTemplate,
            BungieApiConfig bungieApiConfig) {
        this.restTemplate = restTemplate;
        this.bungieApiConfig = bungieApiConfig;
    }

    /**
     * Gets membership Id for the currently logged-in user
     *
     * @return {@link DestinyMembershipResponse}
     * @throws Exception exception
     */
    public DestinyMembershipResponse getMembershipDataForCurrentUser() throws Exception {
        HttpHeaders headers = prepareHttpRequestForBungieRequest();
        var uri = URI.create(bungieApiConfig.getCurrentUserMembershipUrl());
        var httpEntity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, DestinyMembershipResponse.class).getBody();
        } catch (RestClientResponseException rce) {
            throw new Exception("Something went wrong: %s".formatted(rce.getResponseBodyAs(String.class)), rce.getCause());
        }
    }

    /**
     * Get item data for current user
     *
     * @param membershipId   the membershipId of the currently logged-in user
     * @param membershipType the membershipType of the currently logged-in user
     * @return {@link DestinyProfileResponse}
     */
    public DestinyProfileResponse getItemDataForCurrentUser(String membershipId, Integer membershipType) throws Exception {
        var uri = UriComponentsBuilder.fromHttpUrl(bungieApiConfig.getProfileDataUrl())
                .queryParam("components", buildComponentList(List.of(
                        ComponentEnum.CHARACTERS, ComponentEnum.CHARACTER_INVENTORIES)))
                .build(membershipType, membershipId);
        var headers = prepareHttpRequestForBungieRequest();
        var httpEntity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, DestinyProfileResponse.class)
                    .getBody();
        } catch (RestClientException rce) {
            throw new Exception("Something went wrong: %s".formatted(rce.getMessage()), rce);
        }
    }

    private String buildComponentList(List<ComponentEnum> componentList) {
        return componentList.stream()
                .map(ComponentEnum::getCode)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private HttpHeaders prepareHttpRequestForBungieRequest() {
        var accessToken = ((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal().getAttribute("access_token");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FORMAT.formatted(accessToken));
        headers.add(API_KEY_HEADER, bungieApiConfig.getKey());
        return headers;
    }
}
