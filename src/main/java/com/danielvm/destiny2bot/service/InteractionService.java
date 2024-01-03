package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionType.APPLICATION_COMMAND;
import static com.danielvm.destiny2bot.enums.InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.enums.InteractionType;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final MessageRegistry messageRegistry;
  private final AuthorizedMessageRegistry authorizedMessageRegistry;

  public InteractionService(
      AuthorizedMessageRegistry authorizedMessageRegistry,
      MessageRegistry messageRegistry) {
    this.messageRegistry = messageRegistry;
    this.authorizedMessageRegistry = authorizedMessageRegistry;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate
   * message-creator using {@link MessageRegistry} or {@link AuthorizedMessageRegistry} depending on
   * if the given slash command need the user to be authorized
   *
   * @param interaction The received interaction from the Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<InteractionResponse> handleInteraction(Interaction interaction) {
    var interactionType = InteractionType.findByValue(interaction.getType());
    return createInteractionResponse(interaction, interactionType);
  }

  private Mono<InteractionResponse> createInteractionResponse(Interaction interaction,
      InteractionType interactionType) {
    boolean isApplicationCommand = Objects.equals(interactionType, APPLICATION_COMMAND);
    boolean isAutocomplete = Objects.equals(interactionType, APPLICATION_COMMAND_AUTOCOMPLETE);

    if (isAutocomplete) {
      var command = CommandEnum.findByName(interaction.getData().getName());
      return command.isAuthorized() ?
          authorizedMessageRegistry.messageCreator(command).createResponse(interaction) :
          messageRegistry.messageCreator(command).createResponse();
    } else if (isApplicationCommand) {
      var slashCommand = CommandEnum.findByName(interaction.getData().getName());
      return slashCommand.isAuthorized() ?
          authorizedMessageRegistry.messageCreator(slashCommand).createResponse(interaction) :
          messageRegistry.messageCreator(slashCommand).createResponse();
    } else {
      return Mono.just(InteractionResponse.PING());
    }
  }
}
