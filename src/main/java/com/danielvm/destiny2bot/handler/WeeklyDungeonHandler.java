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
public class WeeklyDungeonHandler implements ApplicationCommandSource {

  public static final String MESSAGE_TEMPLATE = """
      This week's dungeon is: %s.
      You have until %s to complete it before the next dungeon in the rotation.
      """;
  private final WeeklyActivitiesService weeklyActivitiesService;

  public WeeklyDungeonHandler(WeeklyActivitiesService weeklyActivitiesService) {
    this.weeklyActivitiesService = weeklyActivitiesService;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    return weeklyActivitiesService.getWeeklyActivity(ActivityMode.DUNGEON)
        .map(weeklyDungeon -> {
          var endDay = MessageUtils.formatDate(weeklyDungeon.getEndDate().toLocalDate());
          return InteractionResponse.builder()
              .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
              .data(InteractionResponseData.builder()
                  .content(MESSAGE_TEMPLATE.formatted(weeklyDungeon.getName(), endDay))
                  .build())
              .build();
        });
  }
}
