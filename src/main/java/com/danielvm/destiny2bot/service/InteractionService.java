package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionType.APPLICATION_COMMAND;

import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.enums.InteractionType;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final MessageRegistryService messageRegistryService;
  private final AutocompleteRegistryService autocompleteRegistryService;

  public InteractionService(
      AutocompleteRegistryService autocompleteRegistryService,
      MessageRegistryService messageRegistryService) {
    this.messageRegistryService = messageRegistryService;
    this.autocompleteRegistryService = autocompleteRegistryService;
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
    boolean isAutocomplete = Objects.equals(interactionType,
        InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE);
    if (isAutocomplete) {
      return Mono.just(
          new InteractionResponse(8,
              InteractionResponseData.builder()
                  .choices(List.of(new Choice("Titan Level 1800", "Titan Level 1800"),
                      new Choice("Titan Level 1800", "Titan Level 1800"))).build()));
    } else if (isApplicationCommand) {
      var slashCommand = CommandEnum.findByName(interaction.getData().getName());
      return messageRegistryService.getMessageCreator(slashCommand).createResponse();
    } else {
      return Mono.just(InteractionResponse.PING());
    }
  }
}
