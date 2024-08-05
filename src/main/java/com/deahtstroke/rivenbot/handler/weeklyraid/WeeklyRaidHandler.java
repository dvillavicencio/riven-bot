package com.deahtstroke.rivenbot.handler.weeklyraid;

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
public class WeeklyRaidHandler implements SlashCommandHandler {

  public static final String MESSAGE_TEMPLATE = """
      This week's raid is: %s.
      You have until %s to complete it before the next raid comes along.
      """;

  private final WeeklyActivitiesService weeklyActivitiesService;

  public WeeklyRaidHandler(WeeklyActivitiesService weeklyActivitiesService) {
    this.weeklyActivitiesService = weeklyActivitiesService;
  }

  @Override
  public SlashCommand getSlashCommand() {
    return SlashCommand.WEEKLY_RAID;
  }

  @Override
  public Mono<InteractionResponse> serve(Interaction interaction) {
    return weeklyActivitiesService.getWeeklyActivity(ActivityMode.RAID)
        .map(activity -> {
          var endDay = MessageUtils.formatDate(activity.getEndDate().toLocalDate());
          return InteractionResponse.builder()
              .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
              .data(InteractionResponseData.builder()
                  .content(MESSAGE_TEMPLATE.formatted(activity.getName(), endDay))
                  .build()).build();
        });
  }
}
