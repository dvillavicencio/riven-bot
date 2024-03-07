package com.danielvm.destiny2bot.handler;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.ActivityMode;
import com.danielvm.destiny2bot.service.WeeklyActivitiesService;
import com.danielvm.destiny2bot.util.MessageUtils;
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
              .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
              .data(InteractionResponseData.builder()
                  .content(MESSAGE_TEMPLATE.formatted(activity.getName(), endDay))
                  .build()).build();
        });
  }
}
