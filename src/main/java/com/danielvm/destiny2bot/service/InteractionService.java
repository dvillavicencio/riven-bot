package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.factory.MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final MessageFactory messageFactory;
  private final RaidDiagramService diagramService;

  public InteractionService(
      MessageFactory messageFactory,
      RaidDiagramService diagramService) {
    this.messageFactory = messageFactory;
    this.diagramService = diagramService;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate
   * message-creator using {@link MessageFactory}
   *
   * @param interaction The received interaction from the Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<InteractionResponse> handleInteraction(Interaction interaction) {
    InteractionType interactionType = InteractionType.findByValue(interaction.getType());
    if (interaction.getData().getName().equals("raid_map")) {
      return diagramService.createResponse(interaction);
    }

    return switch (interactionType) {
      case MESSAGE_COMPONENT, MODAL_SUBMIT -> Mono.just(new InteractionResponse());
      case APPLICATION_COMMAND_AUTOCOMPLETE -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield messageFactory.messageCreator(command).autocompleteResponse(interaction);

      }
      case APPLICATION_COMMAND -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield messageFactory.messageCreator(command).commandResponse(interaction);
      }
      case PING -> InteractionResponse.PING();
    };
  }
}
