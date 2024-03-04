package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.service.RaidInfographicsService;
import com.danielvm.destiny2bot.handler.InteractionHandler;
import com.danielvm.destiny2bot.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@Validated
public class InteractionsController {

  private final InteractionHandler interactionHandler;
  private final RaidInfographicsService raidInfographicsService;
  private final ObjectMapper objectMapper;

  public InteractionsController(
      InteractionHandler interactionHandler,
      RaidInfographicsService raidInfographicsService, ObjectMapper objectMapper) {
    this.interactionHandler = interactionHandler;
    this.raidInfographicsService = raidInfographicsService;
    this.objectMapper = objectMapper;
  }

//  /**
//   * Handles interactions sent from Discord Chat
//   *
//   * @param interaction The interaction sent by Discord
//   * @return Response entity wrapping the appropriate object. If the interaction contains
//   * attachments then the ResponseEntity with a {@link MultiValueMap} with images and their
//   * corresponding bytes, else an {@link InteractionResponse}
//   */
//  @PostMapping("/interactions")
//  public Mono<ResponseEntity<?>> interactions(@RequestBody Interaction interaction) {
//    return interactionHandler.handleInteraction(interaction)
//        .flatMap(response -> {
//          boolean containsAttachments = response.getType() != 1 && CollectionUtils.isNotEmpty(
//              response.getData().getAttachments());
//          return containsAttachments ? multipartFormResponse(interaction, response) :
//              Mono.just(ResponseEntity.ok(response));
//        })
//        .doOnSubscribe(i -> log.info("Received interaction: [{}]", interaction))
//        .doOnSuccess(i -> {
//          try {
//            log.info("Completed interaction: [{}]", objectMapper.writeValueAsString(i));
//          } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//          }
//        });
//  }

  private Mono<ResponseEntity<MultiValueMap<String, HttpEntity<?>>>> multipartFormResponse(
      Interaction interaction, InteractionResponse interactionResponse) {
    try {
      return raidInfographicsService.retrieveEncounterImages(interaction)
          .map(assets -> HttpUtil.prepareMultipartPayload(interactionResponse, assets))
          .map(payload -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
            return new ResponseEntity<>(payload, headers, 200);
          });
    } catch (IOException e) {
      return Mono.error(new RuntimeException(e));
    }
  }
}
