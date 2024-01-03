package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.service.UserAuthorizationService;
import com.danielvm.destiny2bot.util.OAuth2Params;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class RegistrationController {

  private final UserAuthorizationService userAuthorizationService;

  public RegistrationController(
      UserAuthorizationService userAuthorizationService) {
    this.userAuthorizationService = userAuthorizationService;
  }

  /**
   * Handle the callback from Discord during OAuth2 authentication
   *
   * @param authorizationCode the authentication code (short-lived)
   * @return Redirect to start Bungie OAuth2
   */
  @GetMapping("/discord/callback")
  public Mono<ResponseEntity<Object>> handleCallBackFromDiscord(
      @RequestParam(OAuth2Params.CODE) String authorizationCode,
      HttpSession httpSession) {
    return userAuthorizationService
        .authenticateDiscordUser(authorizationCode, httpSession);
  }

  /**
   * Handle the callback from Bungie during OAuth2 authentication
   *
   * @param authorizationCode the authentication code (short-lived)
   * @return Redirect to start Bungie OAuth2
   */
  @GetMapping("/bungie/callback")
  public Mono<ResponseEntity<Object>> handleCallBackFromBungie(
      @RequestParam(OAuth2Params.CODE) String authorizationCode,
      HttpSession httpSession) {
    return userAuthorizationService
        .linkDiscordUserToBungieAccount(authorizationCode, httpSession);
  }

}