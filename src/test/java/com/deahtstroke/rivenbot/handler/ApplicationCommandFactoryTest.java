package com.deahtstroke.rivenbot.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.factory.ApplicationCommandFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationCommandFactoryTest {

  @Mock
  private WeeklyRaidHandler weeklyRaidHandler;
  @Mock
  private WeeklyDungeonHandler weeklyDungeonHandler;
  @Mock
  private RaidStatsHandler raidStatsHandler;
  @InjectMocks
  private ApplicationCommandFactory sut;

  @Test
  @DisplayName("Getting message creator for weekly dungeon command works successfully")
  public void messageCreatorWorksForWeeklyDungeon() {
    // given: a valid command
    SlashCommand command = SlashCommand.WEEKLY_DUNGEON;

    // when: messageCreator is called
    ApplicationCommandSource creator = sut.messageCreator(command);

    // then: the correct message creator is returned
    assertThat(creator)
        .isInstanceOf(WeeklyDungeonHandler.class)
        .isEqualTo(weeklyDungeonHandler);
  }

  @Test
  @DisplayName("Getting message creator for weekly raid command works successfully")
  public void messageCreatorWorksForWeeklyRaid() {
    // given: a valid command
    SlashCommand command = SlashCommand.WEEKLY_RAID;

    // when: messageCreator is called
    ApplicationCommandSource creator = sut.messageCreator(command);

    // then: the correct message creator is returned
    assertThat(creator)
        .isInstanceOf(WeeklyRaidHandler.class)
        .isEqualTo(weeklyRaidHandler);
  }

}
