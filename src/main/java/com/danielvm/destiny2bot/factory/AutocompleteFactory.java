package com.danielvm.destiny2bot.factory;

import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AutocompleteFactory {

  private final Map<SlashCommand, AutocompleteResponseSourceCreator> autocompleteFactory;

  public AutocompleteFactory(
      RaidDiagramMessageCreator raidDiagramMessageCreator,
      RaidStatsMessageCreator raidStatsMessageCreator) {
    this.autocompleteFactory = Map.of(
        SlashCommand.RAID_STATS, raidStatsMessageCreator,
        SlashCommand.RAID_MAP, raidDiagramMessageCreator);
  }

  /**
   * Return the corresponding autocomplete-creator associated with a slash-command
   *
   * @param command The {@link SlashCommand} to get the factory for
   * @return an implementation of {@link AutocompleteResponseSourceCreator}
   * @throws ResourceNotFoundException If no creator is found for the given command
   */
  public AutocompleteResponseSourceCreator messageCreator(SlashCommand command) {
    AutocompleteResponseSourceCreator creator = autocompleteFactory.get(command);
    if (Objects.isNull(creator)) {
      throw new ResourceNotFoundException(
          "No message creator found for command [%s]".formatted(command));
    }
    return creator;
  }
}
