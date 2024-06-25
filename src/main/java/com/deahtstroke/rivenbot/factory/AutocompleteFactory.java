package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import com.deahtstroke.rivenbot.handler.AutocompleteSource;
import com.deahtstroke.rivenbot.handler.RaidStatsHandler;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AutocompleteFactory implements SlashCommandHandler<AutocompleteSource> {

  private final Map<SlashCommand, AutocompleteSource> autocompleteMap;

  public AutocompleteFactory(
      RaidStatsHandler raidStatsHandler) {
    this.autocompleteMap = Map.of(
        SlashCommand.RAID_STATS, raidStatsHandler);
  }

  @Override
  public AutocompleteSource getHandler(SlashCommand command) {
    AutocompleteSource creator = autocompleteMap.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }
}
