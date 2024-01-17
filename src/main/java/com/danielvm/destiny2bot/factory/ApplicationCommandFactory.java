package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import com.danielvm.destiny2bot.factory.creator.ApplicationCommandSource;
import com.danielvm.destiny2bot.factory.creator.AuthorizeMessageCreator;
import com.danielvm.destiny2bot.factory.creator.RaidDiagramMessageCreator;
import com.danielvm.destiny2bot.factory.creator.WeeklyDungeonMessageCreator;
import com.danielvm.destiny2bot.factory.creator.WeeklyRaidMessageCreator;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * This service has a single factory map that contains all the entries between slash-commands and
 * their corresponding message creation services.
 */
@Component
public class ApplicationCommandFactory implements InteractionFactory<ApplicationCommandSource> {

  private final Map<SlashCommand, ApplicationCommandSource> messageFactory;

  public ApplicationCommandFactory(
      RaidDiagramMessageCreator raidDiagramMessageCreator,
      WeeklyRaidMessageCreator weeklyRaidMessageCreator,
      WeeklyDungeonMessageCreator weeklyDungeonMessageCreator,
      AuthorizeMessageCreator authorizeMessageCreator) {
    this.messageFactory = Map.of(
        SlashCommand.WEEKLY_RAID, weeklyRaidMessageCreator,
        SlashCommand.WEEKLY_DUNGEON, weeklyDungeonMessageCreator,
        SlashCommand.AUTHORIZE, authorizeMessageCreator,
        SlashCommand.RAID_MAP, raidDiagramMessageCreator);
  }

  @Override
  public ApplicationCommandSource messageCreator(SlashCommand command) {
    ApplicationCommandSource creator = messageFactory.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }
}
