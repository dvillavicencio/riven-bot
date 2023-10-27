package com.danielvm.destiny2bot.client;

import org.springframework.http.HttpHeaders;

public abstract class BungieClient {

    private static final String BEARER_TOKEN_FORMAT = "Bearer %s";

    private static final String API_KEY_HEADER = "x-api-key";

    /**
     * Prepare Http Headers for authorized requests to Bungie
     *
     * @param apiKey      Bungie app API key
     * @param accessToken End-user access token from OAuth2 flow
     * @return {@link HttpHeaders}
     */
    public HttpHeaders buildAuthorizedHttpRequestHeaders(String apiKey, String accessToken) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FORMAT.formatted(accessToken));
        headers.add(API_KEY_HEADER, apiKey);
        return headers;
    }

    /**
     * Build Http Headers for unauthorized requests to Bungie
     *
     * @param apiKey Bungie app API key
     * @return {@link HttpHeaders}
     */
    public HttpHeaders buildUnauthorizedHttpRequestHeaders(String apiKey) {
        var headers = new HttpHeaders();
        headers.add(API_KEY_HEADER, apiKey);
        return headers;
    }
}
