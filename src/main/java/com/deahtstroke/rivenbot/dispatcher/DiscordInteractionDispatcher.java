package com.deahtstroke.rivenbot.dispatcher;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.exception.BaseException;
import com.deahtstroke.rivenbot.factory.InteractionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DiscordInteractionDispatcher {

  private final InteractionFactory interactionFactory;

  public DiscordInteractionDispatcher(
      InteractionFactory applicationCommandFactory) {
    this.interactionFactory = applicationCommandFactory;
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
        .flatMap(interactionFactory::serve)
        .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromValue(response)))
        .onErrorResume(BaseException.class,
            error -> {
              ProblemDetail problemDetail = ProblemDetail.forStatus(error.getStatus());
              problemDetail.setDetail(error.getMessage());
              return ServerResponse.status(error.getStatus())
                  .body(BodyInserters.fromValue(problemDetail));
            }
        );
  }
}
