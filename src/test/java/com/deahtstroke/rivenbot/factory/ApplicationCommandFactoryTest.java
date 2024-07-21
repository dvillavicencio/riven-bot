package com.deahtstroke.rivenbot.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;
import com.deahtstroke.rivenbot.handler.ApplicationCommandSource;
import com.deahtstroke.rivenbot.handler.RaidStatsHandler;
import com.deahtstroke.rivenbot.handler.WeeklyDungeonHandler;
import com.deahtstroke.rivenbot.handler.WeeklyRaidHandler;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationCommandFactoryTest {

  @Mock
  static WeeklyRaidHandler weeklyRaidHandler;

  @Mock
  static WeeklyDungeonHandler weeklyDungeonHandler;

  @Mock
  static RaidStatsHandler raidStatsHandler;

  @InjectMocks
  ApplicationCommandFactory sut;

  static Stream<Arguments> handlers() {
    return Stream.of(
        Arguments.of(SlashCommand.WEEKLY_RAID, WeeklyRaidHandler.class),
        Arguments.of(SlashCommand.WEEKLY_DUNGEON, WeeklyDungeonHandler.class),
        Arguments.of(SlashCommand.RAID_STATS, RaidStatsHandler.class)
    );
  }

  @ParameterizedTest
  @MethodSource("handlers")
  @DisplayName("Get handler is successful for commands that have a slash command")
  void shouldWorkSuccessfullyForACommand(SlashCommand slashCommand, Class<?> clazz) {
    // when: get handler is invoked for the slash command
    ApplicationCommandSource handler = sut.getHandler(slashCommand);

    // then: the handler is of the expected type
    assertThat(handler).isInstanceOf(clazz);
  }

  @Test
  @DisplayName("Get handler fails for a command that does not have a handler")
  void shouldThrowExceptionForCommandsWithNoHandler() {
    // given: slash command with no handler
    SlashCommand slashCommand = SlashCommand.TEST_COMMAND;

    // when: getHandler is invoked
    // then: the appropriate exception is thrown with the correct message
    org.junit.jupiter.api.Assertions.assertThrows(NoSuchHandlerException.class,
        () -> sut.getHandler(slashCommand),
        "Command with name [TEST_COMMAND] was not found");
  }

}
