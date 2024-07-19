package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.enums.InteractionType;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.BaseException;
import com.deahtstroke.rivenbot.factory.ApplicationCommandFactory;
import com.deahtstroke.rivenbot.factory.AutocompleteFactory;
import com.deahtstroke.rivenbot.factory.MessageComponentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
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

  public InteractionHandler(
      ApplicationCommandFactory applicationCommandFactory,
      AutocompleteFactory autocompleteFactory,
      MessageComponentFactory messageComponentFactory) {
    this.applicationCommandFactory = applicationCommandFactory;
    this.autocompleteFactory = autocompleteFactory;
    this.messageComponentFactory = messageComponentFactory;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate message
   * factory
   *
   * @param request the incoming server request from Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<ServerResponse> resolveRequest(ServerRequest request) {
    return request.bodyToMono(Interaction.class)
        .flatMap(interaction -> {
          InteractionType interactionType = InteractionType.findByValue(interaction.getType());
          return resolveResponse(interaction, interactionType)
              .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromValue(response)))
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
        var handler = messageComponentFactory.getHandler(componentId);
        yield handler.handle(interaction);
      }
      case MODAL_SUBMIT -> Mono.just(new InteractionResponse());
      case APPLICATION_COMMAND_AUTOCOMPLETE -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        var handler = autocompleteFactory.getHandler(command);
        yield handler.handle(interaction);
      }
      case APPLICATION_COMMAND -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        var handler = applicationCommandFactory.getHandler(command);
        yield handler.resolve(interaction);
      }
      case PING -> Mono.just(InteractionResponse.PING());
    };
  }
}
