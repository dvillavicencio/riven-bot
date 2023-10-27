package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieApiConfig;
import com.danielvm.destiny2bot.dto.destiny.profile.CharacterItemResponse;
import com.danielvm.destiny2bot.enums.ComponentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemClient extends BungieClient {

    private final RestTemplate restTemplate;
    private final BungieApiConfig bungieApiConfig;

    public ItemClient(
            RestTemplate restTemplate,
            BungieApiConfig bungieApiConfig) {
        this.restTemplate = restTemplate;
        this.bungieApiConfig = bungieApiConfig;
    }

    /**
     * Get item data for current user
     *
     * @param membershipId   the membershipId of the currently logged-in user
     * @param membershipType the membershipType of the currently logged-in user
     * @return {@link CharacterItemResponse}
     */
    public CharacterItemResponse getItemDataForCurrentUser(String membershipId, Integer membershipType) throws Exception {
        var uri = UriComponentsBuilder.fromHttpUrl(bungieApiConfig.getProfileDataUrl())
                .queryParam("components", buildComponentList(List.of(ComponentEnum.CHARACTER_INVENTORIES)))
                .build(membershipType, membershipId);
        var accessToken = ((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal().getAttribute("access_token");
        var headers = buildAuthorizedHttpRequestHeaders(bungieApiConfig.getKey(), Objects.requireNonNull(accessToken).toString());
        var httpEntity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, CharacterItemResponse.class)
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
}
