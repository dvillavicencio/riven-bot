package com.danielvm.destiny2bot.enums;

import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.stream.Stream;
import lombok.Getter;

public enum SlashCommand {

  AUTHORIZE("authorize", false),
  WEEKLY_DUNGEON("weekly_dungeon", false),
  WEEKLY_RAID("weekly_raid", false),
  RAID_STATS("raid_stats", true),
  RAID_MAP("raid_map", false);

  @Getter
  private final String commandName;

  @Getter
  private final boolean authorized;

  SlashCommand(String commandName, boolean authorized) {
    this.commandName = commandName;
    this.authorized = authorized;
  }

  /**
   * Find a value of this enum by the Command name
   *
   * @param commandName The name of this command
   * @return {@link SlashCommand}
   */
  public static SlashCommand findByName(String commandName) {
    return Stream.of(SlashCommand.values())
        .filter(e -> e.getCommandName().equalsIgnoreCase(commandName))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "Command with name [%s] was not found".formatted(commandName)));
  }

}
