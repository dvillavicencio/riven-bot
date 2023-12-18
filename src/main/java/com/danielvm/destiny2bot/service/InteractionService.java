package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionType.PING;
import static com.danielvm.destiny2bot.enums.InteractionType.findByValue;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.enums.CommandEnum;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final MessageRegistryService commandFactoryService;

  public InteractionService(
      MessageRegistryService messageRegistryService) {
    this.commandFactoryService = messageRegistryService;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate
   * message-creator
   *
   * @param interaction The received interaction from Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<InteractionResponse> handleInteraction(Interaction interaction) {
    var interactionType = findByValue(interaction.getType());
    boolean notPingRequest = !Objects.equals(interactionType, PING);
    if (notPingRequest) {
      var slashCommand = CommandEnum.findByName(interaction.getData().getName());
      return commandFactoryService.getFactory(slashCommand).createResponse();
    } else {
      return Mono.just(InteractionResponse.PING());
    }
  }
}
