package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieApiConfig;
import com.danielvm.destiny2bot.dto.destiny.profile.CharacterInfoResponse;
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

import java.util.Objects;

@Component
@Slf4j
public class CharacterClient extends BungieClient {

    private final RestTemplate restTemplate;
    private final BungieApiConfig bungieApiConfig;

    public CharacterClient(RestTemplate restTemplate, BungieApiConfig bungieApiConfig) {
        this.restTemplate = restTemplate;
        this.bungieApiConfig = bungieApiConfig;
    }

    /**
     * Get all character details for the current user
     *
     * @param membershipId   The user membershipId
     * @param membershipType The user membershipType (Which platform they play on)
     * @return {@link CharacterInfoResponse}
     * @throws Exception If something goes wrong, throw generic exception (TBD which error code should throw)
     */
    public CharacterInfoResponse getDetailsPerCharacter(String membershipId, Integer membershipType) throws Exception {
        var uri = UriComponentsBuilder.fromHttpUrl(bungieApiConfig.getProfileDataUrl())
                .queryParam("components", ComponentEnum.CHARACTERS.getCode())
                .build(membershipType, membershipId);
        var accessToken = ((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal().getAttribute("access_token");
        var headers = buildAuthorizedHttpRequestHeaders(bungieApiConfig.getKey(), Objects.requireNonNull(accessToken).toString());
        var httpEntity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, CharacterInfoResponse.class)
                    .getBody();
        } catch (RestClientException rce) {
            throw new Exception("Something went wrong: %s".formatted(rce.getMessage()), rce);
        }
    }
}
