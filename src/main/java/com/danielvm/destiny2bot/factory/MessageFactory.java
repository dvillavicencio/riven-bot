package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * This service has a single factory map that contains all the entries between slash-commands and
 * their corresponding message creation services.
 */
@Component
public class MessageFactory {

  private final Map<CommandEnum, MessageResponse> messageFactory;

  public MessageFactory(
      WeeklyRaidMessageCreator weeklyRaidMessageCreator,
      WeeklyDungeonMessageCreator weeklyDungeonMessageCreator,
      AuthorizeMessageCreator authorizeMessageCreator) {
    this.messageFactory = Map.of(
        CommandEnum.WEEKLY_RAID, weeklyRaidMessageCreator,
        CommandEnum.WEEKLY_DUNGEON, weeklyDungeonMessageCreator,
        CommandEnum.AUTHORIZE, authorizeMessageCreator);
  }

  /**
   * Return the corresponding message-creator associated with a Command in {@link CommandEnum}
   *
   * @param command The command to get the factory for
   * @return an implementation of {@link MessageResponse}
   * @throws ResourceNotFoundException If no creator is found for the given command
   */
  public MessageResponse messageCreator(CommandEnum command) {
    MessageResponse creator = messageFactory.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }
}
