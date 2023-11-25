package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.config.BungieConfiguration;
import com.danielvm.destiny2bot.service.UserAuthorizationService;
import com.danielvm.destiny2bot.util.OAuth2Params;
import com.danielvm.destiny2bot.util.OAuth2Util;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RegistrationController {

  private final UserAuthorizationService userAuthorizationService;
  private final BungieConfiguration bungieConfiguration;

  public RegistrationController(
      UserAuthorizationService userAuthorizationService,
      BungieConfiguration bungieConfiguration) {
    this.userAuthorizationService = userAuthorizationService;
    this.bungieConfiguration = bungieConfiguration;
  }

  /**
   * Handle the callback from Discord during OAuth2 authentication
   *
   * @param authorizationCode the authentication code (short-lived)
   * @return Redirect to start Bungie OAuth2
   */
  @GetMapping("/discord/callback")
  public ResponseEntity<Void> handleCallBackFromDiscord(
      @RequestParam(OAuth2Params.CODE) String authorizationCode,
      HttpSession httpSession) {
    userAuthorizationService.authenticateDiscordUser(authorizationCode, httpSession);
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION,
            OAuth2Util.bungieAuthorizationUrl(bungieConfiguration))
        .build();
  }

  /**
   * Handle the callback from Bungie during OAuth2 authentication
   *
   * @param authorizationCode the authentication code (short-lived)
   * @return Redirect to start Bungie OAuth2
   */
  @GetMapping("/bungie/callback")
  public ResponseEntity<Void> handleCallBackFromBungie(
      @RequestParam(OAuth2Params.CODE) String authorizationCode,
      HttpSession httpSession) {
    userAuthorizationService.linkDiscordUserToBungieAccount(authorizationCode, httpSession);
    return ResponseEntity.noContent().build();
  }

}
