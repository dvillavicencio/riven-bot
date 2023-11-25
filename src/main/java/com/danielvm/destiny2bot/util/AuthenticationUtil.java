package com.danielvm.destiny2bot.util;

public class AuthenticationUtil {

  private static final String BEARER_TOKEN_FORMAT = "Bearer %s";

  private AuthenticationUtil() {
  }

  /**
   * Format the given token to bearer token format
   *
   * @param token The token to format
   * @return The formatted String
   */
  public static String formatBearerToken(String token) {
    return BEARER_TOKEN_FORMAT.formatted(token);
  }
}
