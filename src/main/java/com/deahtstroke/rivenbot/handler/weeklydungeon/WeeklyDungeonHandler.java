package com.deahtstroke.rivenbot.handler.weeklydungeon;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.ActivityMode;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.handler.SlashCommandHandler;
import com.deahtstroke.rivenbot.service.WeeklyActivitiesService;
import com.deahtstroke.rivenbot.util.MessageUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WeeklyDungeonHandler implements SlashCommandHandler {

  public static final String MESSAGE_TEMPLATE = """
      This week's dungeon is: %s.
      You have until %s to complete it before the next dungeon in the rotation.
      """;
  private final WeeklyActivitiesService weeklyActivitiesService;

  public WeeklyDungeonHandler(WeeklyActivitiesService weeklyActivitiesService) {
    this.weeklyActivitiesService = weeklyActivitiesService;
  }

  @Override
  public SlashCommand getSlashCommand() {
    return SlashCommand.WEEKLY_DUNGEON;
  }

  @Override
  public Mono<InteractionResponse> serve(Interaction interaction) {
    return weeklyActivitiesService.getWeeklyActivity(ActivityMode.DUNGEON)
        .map(weeklyDungeon -> {
          var endDay = MessageUtils.formatDate(weeklyDungeon.getEndDate().toLocalDate());
          return InteractionResponse.builder()
              .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
              .data(InteractionResponseData.builder()
                  .content(MESSAGE_TEMPLATE.formatted(weeklyDungeon.getName(), endDay))
                  .build())
              .build();
        });
  }
}
