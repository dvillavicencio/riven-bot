package com.deahtstroke.rivenbot.enums;

import java.util.NoSuchElementException;
import java.util.stream.Stream;
import lombok.Getter;

public enum SlashCommand {

  WEEKLY_DUNGEON("weekly_dungeon"),
  WEEKLY_RAID("weekly_raid"),
  RAID_STATS("raid_stats"),
  ABOUT("about"),
  TEST_COMMAND("test_command");

  @Getter
  private final String commandName;

  SlashCommand(String commandName) {
    this.commandName = commandName;
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
        .orElseThrow(() -> new NoSuchElementException(
            "Command with name [%s] was not found".formatted(commandName)));
  }

}
