package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.factory.AuthorizedMessageFactory;
import com.danielvm.destiny2bot.factory.MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final MessageFactory messageFactory;
  private final AuthorizedMessageFactory authorizedMessageFactory;
  private final RaidDiagramService diagramService;

  public InteractionService(
      AuthorizedMessageFactory authorizedMessageFactory,
      MessageFactory messageFactory,
      RaidDiagramService diagramService) {
    this.messageFactory = messageFactory;
    this.authorizedMessageFactory = authorizedMessageFactory;
    this.diagramService = diagramService;
  }

  private static String verifyAndRetrieveUserId(Interaction interaction,
      InteractionType interactionType, CommandEnum command) {
    Assert.notNull(interaction.getMember().getUser().getId(),
        "Member information is null for given command [%s] and command type [%s]"
            .formatted(command.getCommandName(), interactionType));

    return interaction.getMember().getUser().getId();
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate
   * message-creator using {@link MessageFactory} or {@link AuthorizedMessageFactory} depending on
   * if the given slash command needs the user to be authorized
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
        CommandEnum command = CommandEnum.findByName(interaction.getData().getName());
        if (command.isAuthorized()) {
          String userId = verifyAndRetrieveUserId(interaction, interactionType, command);
          yield authorizedMessageFactory.messageCreator(command).autocompleteResponse(userId);
        } else {
          yield messageFactory.messageCreator(command).autocompleteResponse();
        }
      }
      case APPLICATION_COMMAND -> {
        CommandEnum command = CommandEnum.findByName(interaction.getData().getName());
        if (command.isAuthorized()) {
          String userId = verifyAndRetrieveUserId(interaction, interactionType, command);
          yield authorizedMessageFactory.messageCreator(command).commandResponse(userId);
        } else {
          yield messageFactory.messageCreator(command).commandResponse();
        }
      }
      case PING -> InteractionResponse.PING();
    };
  }
}
