package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.annotation.ValidSignature;
import com.danielvm.destiny2bot.dto.discord.interactions.Interaction;
import com.danielvm.destiny2bot.service.InteractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@RestController
@Validated
public class InteractionsController {

  private final InteractionService interactionService;

  public InteractionsController(
      InteractionService interactionService) {
    this.interactionService = interactionService;
  }

  /**
   * Handles interactions sent from Discord Chat
   *
   * @param interaction The interaction sent by Discord
   * @param request     This request is used only to validate the signature of the message
   * @return TBD
   */
  @PostMapping("/interactions")
  public ResponseEntity<?> interactions(
      @RequestBody Interaction interaction,
      @ValidSignature ContentCachingRequestWrapper request) {
    log.info("Interaction received [{}]", interaction);
    var response = interactionService.handleInteraction(interaction);
    log.info("Interaction completed [{}]", interaction);
    return ResponseEntity.ok(response);
  }
}
