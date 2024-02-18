package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import com.danielvm.destiny2bot.factory.handler.AutocompleteSource;
import com.danielvm.destiny2bot.factory.handler.RaidMapHandler;
import com.danielvm.destiny2bot.factory.handler.RaidStatsHandler;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AutocompleteFactory implements SlashCommandHandler<AutocompleteSource> {

  private final Map<SlashCommand, AutocompleteSource> autocompleteFactory;

  public AutocompleteFactory(
      RaidMapHandler raidMapHandler,
      RaidStatsHandler raidStatsHandler) {
    this.autocompleteFactory = Map.of(
        SlashCommand.RAID_MAP, raidMapHandler,
        SlashCommand.RAID_STATS, raidStatsHandler);
  }

  @Override
  public AutocompleteSource messageCreator(SlashCommand command) {
    AutocompleteSource creator = autocompleteFactory.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }
}
