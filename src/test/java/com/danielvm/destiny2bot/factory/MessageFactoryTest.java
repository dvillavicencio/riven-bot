package com.danielvm.destiny2bot.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageFactoryTest {

  @Mock
  private WeeklyRaidMessageCreator weeklyRaidMessageCreator;
  @Mock
  private WeeklyDungeonMessageCreator weeklyDungeonMessageCreator;
  @Mock
  private AuthorizeMessageCreator authorizeMessageCreator;
  @Mock
  private RaidDiagramMessageCreator raidDiagramMessageCreator;
  @InjectMocks
  private MessageFactory sut;

  @Test
  @DisplayName("Getting message creator for authorize command works successfully")
  public void messageCreatorWorksForAuthorize() {
    // given: a valid command
    SlashCommand command = SlashCommand.AUTHORIZE;

    // when: messageCreator is called
    CommandResponseCreator creator = sut.messageCreator(command);

    // then: the correct message creator is returned
    assertThat(creator)
        .isInstanceOf(AuthorizeMessageCreator.class)
        .isEqualTo(authorizeMessageCreator);
  }

  @Test
  @DisplayName("Getting message creator for weekly dungeon command works successfully")
  public void messageCreatorWorksForWeeklyDungeon() {
    // given: a valid command
    SlashCommand command = SlashCommand.WEEKLY_DUNGEON;

    // when: messageCreator is called
    CommandResponseCreator creator = sut.messageCreator(command);

    // then: the correct message creator is returned
    assertThat(creator)
        .isInstanceOf(WeeklyDungeonMessageCreator.class)
        .isEqualTo(weeklyDungeonMessageCreator);
  }

  @Test
  @DisplayName("Getting message creator for weekly raid command works successfully")
  public void messageCreatorWorksForWeeklyRaid() {
    // given: a valid command
    SlashCommand command = SlashCommand.WEEKLY_RAID;

    // when: messageCreator is called
    CommandResponseCreator creator = sut.messageCreator(command);

    // then: the correct message creator is returned
    assertThat(creator)
        .isInstanceOf(WeeklyRaidMessageCreator.class)
        .isEqualTo(weeklyRaidMessageCreator);
  }

  @Test
  @DisplayName("Getting message creator fails for authorized and invalid commands")
  public void messageCreatorFails() {
    // given: an authorized command
    SlashCommand command = SlashCommand.RAID_STATS;

    // when: messageCreator is called an exception is thrown
    Assertions.assertThrows(ResourceNotFoundException.class,
        () -> sut.messageCreator(command),
        "No message creator found for command [%s]".formatted(command));
  }

}
