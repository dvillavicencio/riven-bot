package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.annotation.ValidSignature;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.service.InteractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;
import reactor.core.publisher.Mono;

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
  public Mono<?> interactions(
      @RequestBody Interaction interaction,
      @ValidSignature ContentCachingRequestWrapper request) {
    return interactionService.handleInteraction(interaction)
        .doOnSubscribe(i -> log.info("Received interaction: [{}]", interaction))
        .doOnSuccess(i -> log.info("Completed retrieving response for interaction: [{}]", i));
  }
}
