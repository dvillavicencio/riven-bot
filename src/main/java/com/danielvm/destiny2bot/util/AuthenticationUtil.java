package com.danielvm.destiny2bot.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.Assert;

public class AuthenticationUtil {

    private static final String BEARER_TOKEN_FORMAT = "Bearer %s";
    private static final String ACCESS_TOKEN_ATTRIBUTE_NAME = "access_token";

    private AuthenticationUtil() {
    }

    /**
     * Return the access_token in Bearer token format
     *
     * @param authentication The authenticated user
     * @return The Bearer token
     */
    public static String getBearerToken(Authentication authentication) {
        Assert.isTrue(authentication instanceof OAuth2AuthenticationToken,
                "Authenticated User does not have an access token with Bungie");
        var accessToken = ((OAuth2AuthenticationToken) authentication)
                .getPrincipal().getAttribute(ACCESS_TOKEN_ATTRIBUTE_NAME);
        return BEARER_TOKEN_FORMAT.formatted(accessToken);
    }
}
