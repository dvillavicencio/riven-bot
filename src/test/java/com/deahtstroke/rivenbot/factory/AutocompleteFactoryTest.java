package com.deahtstroke.rivenbot.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;
import com.deahtstroke.rivenbot.handler.AutocompleteSource;
import com.deahtstroke.rivenbot.handler.RaidStatsHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutocompleteFactoryTest {

  @Mock
  RaidStatsHandler raidStatsHandler;

  @InjectMocks
  AutocompleteFactory sut;

  @Test
  @DisplayName("Get handler works for a particular slash command")
  void shouldWorkForSlashCommand() {
    // given: a slash command
    SlashCommand slashCommand = SlashCommand.RAID_STATS;

    // when: get handler is called
    AutocompleteSource handler = sut.getHandler(slashCommand);

    // then: the handler is the expected message handler
    assertThat(handler).isInstanceOf(raidStatsHandler.getClass());
  }

  @Test
  @DisplayName("Get handler works for a particular slash command")
  void shouldThrowExceptionOnNoHandlerFound() {
    // given: a slash command
    SlashCommand slashCommand = SlashCommand.WEEKLY_RAID;

    // when: get handler is called
    // then: a NoSuchHandlerException is thrown with the correct error message
    Assertions.assertThrows(NoSuchHandlerException.class,
        () -> sut.getHandler(slashCommand),
        "No message creator found for command [RAID_MAP]");
  }

}
