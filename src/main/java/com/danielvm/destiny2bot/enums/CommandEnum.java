package com.danielvm.destiny2bot.enums;

import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.stream.Stream;
import lombok.Getter;

public enum CommandEnum {

  AUTHORIZE("authorize"),
  WEEKLY_DUNGEON("weekly_dungeon"),
  WEEKLY_RAID("weekly_raid"),
  RAID_STATS("raid_stats");

  @Getter
  private final String commandName;

  CommandEnum(String commandName) {
    this.commandName = commandName;
  }

  /**
   * Find a value of this enum by the Command name
   *
   * @param commandName The name of this command
   * @return {@link CommandEnum}
   */
  public static CommandEnum findByName(String commandName) {
    return Stream.of(CommandEnum.values())
        .filter(e -> e.getCommandName().equalsIgnoreCase(commandName))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "Command with name [%s] was not found".formatted(
                commandName))); // this should never happen
  }

}
