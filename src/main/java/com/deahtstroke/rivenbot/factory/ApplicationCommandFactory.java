package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import com.deahtstroke.rivenbot.handler.ApplicationCommandSource;
import com.deahtstroke.rivenbot.handler.RaidMapHandler;
import com.deahtstroke.rivenbot.handler.RaidStatsHandler;
import com.deahtstroke.rivenbot.handler.WeeklyDungeonHandler;
import com.deahtstroke.rivenbot.handler.WeeklyRaidHandler;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * This service has a single factory map that contains all the entries between slash-commands and
 * their corresponding message creation services.
 */
@Component
public class ApplicationCommandFactory implements SlashCommandHandler<ApplicationCommandSource> {

  private final Map<SlashCommand, ApplicationCommandSource> messageFactory;

  public ApplicationCommandFactory(
      RaidMapHandler raidMapHandler,
      WeeklyRaidHandler weeklyRaidHandler,
      WeeklyDungeonHandler weeklyDungeonHandler,
      RaidStatsHandler raidStatsHandler) {
    this.messageFactory = Map.of(
        SlashCommand.WEEKLY_RAID, weeklyRaidHandler,
        SlashCommand.WEEKLY_DUNGEON, weeklyDungeonHandler,
        SlashCommand.RAID_MAP, raidMapHandler,
        SlashCommand.RAID_STATS, raidStatsHandler);
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
