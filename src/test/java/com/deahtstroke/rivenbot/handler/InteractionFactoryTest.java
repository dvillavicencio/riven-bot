package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.handler.about.AboutHandler;
import com.deahtstroke.rivenbot.handler.raidstats.RaidStatsCommandHandler;
import com.deahtstroke.rivenbot.handler.weeklydungeon.WeeklyDungeonHandler;
import com.deahtstroke.rivenbot.handler.weeklyraid.WeeklyRaidHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractionFactoryTest {

  @Mock
  AboutHandler aboutHandler;

  @Mock
  RaidStatsCommandHandler raidStatsCommandHandler;

  @Mock
  WeeklyDungeonHandler weeklyDungeonHandler;

  @Mock
  WeeklyRaidHandler weeklyRaidHandler;

  @Mock


  List<SlashCommandHandler> slashCommandHandlers = List.of(aboutHandler, raidStatsCommandHandler,
      weeklyRaidHandler, weeklyDungeonHandler);

  @InjectMocks
  InteractionFactory sut;

}
