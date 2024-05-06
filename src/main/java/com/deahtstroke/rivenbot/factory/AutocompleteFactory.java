package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import com.deahtstroke.rivenbot.handler.AutocompleteSource;
import com.deahtstroke.rivenbot.handler.RaidMapHandler;
import com.deahtstroke.rivenbot.handler.RaidStatsHandler;
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
