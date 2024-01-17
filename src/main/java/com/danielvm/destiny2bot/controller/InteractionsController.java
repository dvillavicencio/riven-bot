package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.annotation.ValidSignature;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.service.InteractionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  public InteractionsController(
      InteractionService interactionService, ObjectMapper objectMapper) {
    this.interactionService = interactionService;
    this.objectMapper = objectMapper;
  }

  /**
   * Handles interactions sent from Discord Chat
   *
   * @param interaction The interaction sent by Discord
   * @param request     This request is used only to validate the signature of the message
   * @return TBD
   */
  @PostMapping("/interactions")
  public Mono<InteractionResponse> interactions(
      @RequestBody Interaction interaction,
      @ValidSignature ContentCachingRequestWrapper request) throws JsonProcessingException {
    log.info("Interaction received: [{}]", objectMapper.writeValueAsString(interaction));
    return interactionService.handleInteraction(interaction)
        .doOnSubscribe(i -> log.info("Received interaction: [{}]", interaction))
        .doOnSuccess(i -> {
          try {
            log.info("Completed retrieving response for interaction: [{}]",
                objectMapper.writeValueAsString(i));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
