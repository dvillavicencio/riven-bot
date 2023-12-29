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

  private final MessageRegistryService messageRegistryService;
  private final AuthorizedMessageRegistryService authorizedMessageRegistryService;

  public InteractionService(
      AuthorizedMessageRegistryService authorizedMessageRegistryService,
      MessageRegistryService messageRegistryService) {
    this.messageRegistryService = messageRegistryService;
    this.authorizedMessageRegistryService = authorizedMessageRegistryService;
  }

  /**
   * Handles the incoming interactions from Discord using each slash-command's appropriate
   * message-creator
   *
   * @param interaction The received interaction from Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<InteractionResponse> handleInteraction(Interaction interaction) {
    var interactionType = InteractionType.findByValue(interaction.getType());
    boolean isApplicationCommand = Objects.equals(interactionType, APPLICATION_COMMAND);
    boolean isAutocomplete = Objects.equals(interactionType, APPLICATION_COMMAND_AUTOCOMPLETE);
    if (isAutocomplete) {
      var slashCommand = CommandEnum.findByName(interaction.getData().getName());
      return authorizedMessageRegistryService.getMessageCreator(slashCommand)
          .createResponse(interaction);
    } else if (isApplicationCommand) {
      var slashCommand = CommandEnum.findByName(interaction.getData().getName());
      return messageRegistryService.getMessageCreator(slashCommand).createResponse();
    } else {
      return Mono.just(InteractionResponse.PING());
    }
  }
}
