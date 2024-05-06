package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.InteractionType;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.BaseException;
import com.deahtstroke.rivenbot.factory.ApplicationCommandFactory;
import com.deahtstroke.rivenbot.factory.AutocompleteFactory;
import com.deahtstroke.rivenbot.factory.MessageComponentFactory;
import com.deahtstroke.rivenbot.service.RaidInfographicsService;
import com.deahtstroke.rivenbot.util.HttpResponseUtils;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionHandler {

  private final ApplicationCommandFactory applicationCommandFactory;
  private final AutocompleteFactory autocompleteFactory;
  private final MessageComponentFactory messageComponentFactory;
  private final RaidInfographicsService raidInfographicsService;

  public InteractionHandler(
      ApplicationCommandFactory applicationCommandFactory,
      AutocompleteFactory autocompleteFactory,
      MessageComponentFactory messageComponentFactory,
      RaidInfographicsService raidInfographicsService) {
    this.applicationCommandFactory = applicationCommandFactory;
    this.autocompleteFactory = autocompleteFactory;
    this.messageComponentFactory = messageComponentFactory;
    this.raidInfographicsService = raidInfographicsService;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate message
   * factory
   *
   * @param request the incoming server request from Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<ServerResponse> handle(ServerRequest request) {
    return request.bodyToMono(Interaction.class)
        .flatMap(interaction -> {
          InteractionType interactionType = InteractionType.findByValue(interaction.getType());
          ParameterizedTypeReference<MultiValueMap<String, HttpEntity<?>>> multiValueReference =
              new ParameterizedTypeReference<>() {
              };

          Mono<InteractionResponse> interactionResponse = resolveResponse(interaction,
              interactionType);
          return interactionResponse
              .flatMap(response -> {
                boolean hasAttachments =
                    !Objects.equals(response.getType(), InteractionResponseType.PONG.getType())
                    && CollectionUtils.isNotEmpty(response.getData().getAttachments());

                return ServerResponse.ok().body(hasAttachments ?
                    BodyInserters.fromProducer(attachmentsResponse(interaction, response),
                        multiValueReference) :
                    BodyInserters.fromValue(response));
              })
              .onErrorResume(BaseException.class,
                  error -> {
                    ProblemDetail problemDetail = ProblemDetail.forStatus(error.getStatus());
                    problemDetail.setDetail(error.getMessage());
                    return ServerResponse.status(error.getStatus())
                        .body(BodyInserters.fromValue(problemDetail));
                  }
              );
        });
  }

  private Mono<InteractionResponse> resolveResponse(Interaction interaction,
      InteractionType interactionType) {
    return switch (interactionType) {
      case MESSAGE_COMPONENT -> {
        String componentId = interaction.getData().getCustomId();
        yield messageComponentFactory.handle(componentId).respond(interaction);
      }
      case MODAL_SUBMIT -> Mono.just(new InteractionResponse());
      case APPLICATION_COMMAND_AUTOCOMPLETE -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield autocompleteFactory.messageCreator(command).autocompleteResponse(interaction);
      }
      case APPLICATION_COMMAND -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield applicationCommandFactory.messageCreator(command).createResponse(interaction);
      }
      case PING -> Mono.just(InteractionResponse.PING());
    };
  }

  private Mono<MultiValueMap<String, HttpEntity<?>>> attachmentsResponse(
      Interaction interaction, InteractionResponse interactionResponse) {
    try {
      return raidInfographicsService.retrieveEncounterImages(interaction)
          .map(assets -> HttpResponseUtils.filesResponse(interactionResponse, assets));
    } catch (IOException e) {
      return Mono.error(new RuntimeException(e));
    }
  }

}
