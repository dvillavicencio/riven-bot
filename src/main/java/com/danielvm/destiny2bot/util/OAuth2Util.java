package com.danielvm.destiny2bot.util;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class OAuth2Util {

    private OAuth2Util() {
    }

    /**
     * Build body parameters for a token request to an OAuth2 resource provider
     *
     * @param authorizationCode The authorization code
     * @param redirectUri       The redirect type
     * @param clientSecret      The client secret
     * @param clientId          The clientId
     * @return {@link MultiValueMap} of OAuth2 attributes for Token exchange
     */
    public static MultiValueMap<String, String> buildTokenExchangeParameters(
            String authorizationCode, String redirectUri, String clientSecret, String clientId) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(OAuth2Params.CODE, authorizationCode);
        map.add(OAuth2Params.GRANT_TYPE, OAuth2Params.AUTHORIZATION_CODE);
        map.add(OAuth2Params.REDIRECT_URI, redirectUri);
        map.add(OAuth2Params.CLIENT_SECRET, clientSecret);
        map.add(OAuth2Params.CLIENT_ID, clientId);
        return map;
    }

    /**
     * Build body parameters for a token request to an OAuth2 resource provider
     *
     * @param refreshToken The refresh token to send
     * @return {@link MultiValueMap} of OAuth2 attributes for refresh token exchange
     */
    public static MultiValueMap<String, String> buildRefreshTokenExchangeParameters(
            String refreshToken, String clientId, String clientSecret) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(OAuth2Params.GRANT_TYPE, OAuth2Params.REFRESH_TOKEN);
        map.add(OAuth2Params.REFRESH_TOKEN, refreshToken);
        map.add(OAuth2Params.CLIENT_ID, clientId);
        map.add(OAuth2Params.CLIENT_SECRET, clientSecret);
        return map;
    }
}
