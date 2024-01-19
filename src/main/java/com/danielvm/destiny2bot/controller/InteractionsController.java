package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.annotation.ValidSignature;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.service.ImageAssetService;
import com.danielvm.destiny2bot.service.InteractionService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
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
  private final ImageAssetService imageAssetService;

  public InteractionsController(
      InteractionService interactionService,
      ImageAssetService imageAssetService) {
    this.interactionService = interactionService;
    this.imageAssetService = imageAssetService;
  }

  /**
   * Handles interactions sent from Discord Chat
   *
   * @param interaction The interaction sent by Discord
   * @param request     This request is used only to validate the signature of the message
   * @return TBD
   */
  @PostMapping("/interactions")
  public Mono<Object> interactions(
      @RequestBody Interaction interaction,
      @ValidSignature ContentCachingRequestWrapper request) {
    return interactionService.handleInteraction(interaction)
        .flatMap(interactionResponse -> {
          if (interactionResponse.getType() != 1 && CollectionUtils.isNotEmpty(
              interactionResponse.getData()
                  .getAttachments())) {
            try {
              return imageAssetService.retrieveEncounterImages(interaction)
                  .map(assets -> {
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("payload_json", interactionResponse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"payload_json\"");
                    assets.forEach((key, value) -> {
                      try {
                        builder.part("files[%s]".formatted(key), value.getContentAsByteArray())
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                "form-data; name=\"files[%s]\"; filename=\"%s\"".formatted(key,
                                    value.getFilename()))
                            .contentType(
                                MediaType.valueOf(
                                    "image/" + FilenameUtils.getExtension(value.getFilename())));
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    });
                    return builder.build();
                  })
                  .map(body -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
                    return Mono.just(new ResponseEntity<>(body, headers, 200));
                  });
            } catch (IOException e) {
              return Mono.error(new RuntimeException(e));
            }
          }
          return Mono.just(interactionResponse);
        })
        .doOnSubscribe(i -> log.info("Received interaction: [{}]", interaction))
        .doOnSuccess(i -> log.info("Completed retrieving response for interaction: [{}]", i));
  }

  @PostMapping("/assets")
  public Mono<Object> getAssets(@RequestBody Interaction interaction)
      throws IOException {
    return imageAssetService.retrieveEncounterImages(interaction)
        .map(assets -> {
          MultipartBodyBuilder builder = new MultipartBodyBuilder();
          assets.forEach((key, value) -> {
            try {
              builder.part("files[%s]".formatted(key), value.getContentAsByteArray())
                  .header(HttpHeaders.CONTENT_DISPOSITION, "form-data",
                      "name=\"files[%s]\"".formatted(key),
                      "filename=\"%s\"".formatted(value.getFilename()))
                  .contentType(
                      MediaType.valueOf(
                          "image/" + FilenameUtils.getExtension(value.getFilename())));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
          return builder.build();
        })
        .map(map -> {
          HttpHeaders headers = new HttpHeaders();
          headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
          return new ResponseEntity<>(map, headers, 200);
        });
  }
}
