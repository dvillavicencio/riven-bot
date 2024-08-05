package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.enums.InteractionType;
import com.deahtstroke.rivenbot.enums.MessageComponentId;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;
import com.deahtstroke.rivenbot.handler.AutocompleteHandler;
import com.deahtstroke.rivenbot.handler.Handler;
import com.deahtstroke.rivenbot.handler.MessageComponentHandler;
import com.deahtstroke.rivenbot.handler.SlashCommandHandler;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class InteractionFactory {

  private final List<SlashCommandHandler> slashCommandHandlers;
  private final List<MessageComponentHandler> messageComponentHandlers;
  private final List<AutocompleteHandler> autocompleteHandlers;

  private final EnumMap<SlashCommand, SlashCommandHandler> slashCommandFactory =
      new EnumMap<>(SlashCommand.class);
  private final EnumMap<MessageComponentId, MessageComponentHandler> messageComponentFactory =
      new EnumMap<>(MessageComponentId.class);
  private final EnumMap<SlashCommand, AutocompleteHandler> autocompleteFactory =
      new EnumMap<>(SlashCommand.class);

  public InteractionFactory(
      List<SlashCommandHandler> slashCommandHandlers,
      List<MessageComponentHandler> messageComponentHandlers,
      List<AutocompleteHandler> autocompleteHandlers) {
    this.slashCommandHandlers = slashCommandHandlers;
    this.messageComponentHandlers = messageComponentHandlers;
    this.autocompleteHandlers = autocompleteHandlers;
  }

  @PostConstruct
  private void init() {
    for (SlashCommandHandler handler : slashCommandHandlers) {
      slashCommandFactory.put(handler.getSlashCommand(), handler);
    }

    for (MessageComponentHandler handler : messageComponentHandlers) {
      messageComponentFactory.put(handler.getComponentId(), handler);
    }

    for (AutocompleteHandler handler : autocompleteHandlers) {
      autocompleteFactory.put(handler.getSlashCommand(), handler);
    }
  }

  /**
   * This method returns and routes a given Discord interaction to the correct {@link Handler} class
   * that contains all the business logic. It takes into account interaction type, slash-command,
   * and message component ID if given
   *
   * @param interaction the Discord interaction
   * @return the correct {@link Handler} for the given interaction
   */
  public Mono<InteractionResponse> serve(Interaction interaction) {
    InteractionType interactionType = InteractionType.findByValue(interaction.getType());
    if (Objects.equals(interactionType, InteractionType.PING)) {
      return Mono.just(InteractionResponse.pingResponse());
    }
    Handler handler = switch (interactionType) {
      case MESSAGE_COMPONENT -> {
        MessageComponentId componentId = MessageComponentId.findById(
            interaction.getData().getCustomId());
        yield messageComponentFactory.get(componentId);
      }
      case APPLICATION_COMMAND_AUTOCOMPLETE -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield autocompleteFactory.get(command);
      }
      case APPLICATION_COMMAND -> {
        SlashCommand command = SlashCommand.findByName(interaction.getData().getName());
        yield slashCommandFactory.get(command);
      }
      case PING, MODAL_SUBMIT -> null;
    };
    if (Objects.isNull(handler)) {
      log.error("No handler found for interaction type [{}] and interaction [{}]", interactionType,
          interaction);
      throw new NoSuchHandlerException("No handler found");
    }
    return handler.serve(interaction);
  }
}
