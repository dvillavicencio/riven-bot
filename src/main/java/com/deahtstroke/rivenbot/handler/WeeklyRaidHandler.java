package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.ActivityMode;
import com.deahtstroke.rivenbot.service.WeeklyActivitiesService;
import com.deahtstroke.rivenbot.util.MessageUtils;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WeeklyRaidHandler implements ApplicationCommandSource {

  public static final String MESSAGE_TEMPLATE = """
      This week's raid is: %s.
      You have until %s to complete it before the next raid comes along.
      """;

  private final WeeklyActivitiesService weeklyActivitiesService;

  public WeeklyRaidHandler(WeeklyActivitiesService weeklyActivitiesService) {
    this.weeklyActivitiesService = weeklyActivitiesService;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
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
