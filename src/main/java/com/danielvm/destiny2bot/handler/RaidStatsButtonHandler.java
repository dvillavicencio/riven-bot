package com.danielvm.destiny2bot.handler;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import com.danielvm.destiny2bot.util.MessageUtil;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RaidStatsButtonHandler implements MessageComponentSource {

  private final static String RAID_STATS_EXPLANATION = """
      These aggregated raid statistics represent some crunched numbers Bungie has put out for you \
      such as total kills and total deaths in a raid. All the stats you are seeing right now are \
      an aggregated total throughout **all your characters for a single raid**.
            
      Please note that the ":first_place: fastest" stat as of now is calculated only on full raid clears. \
      That means raids that did not start from the beginning **do not count**.""";

  @Override
  public Mono<InteractionResponse> respond(Interaction interaction) {
    return Mono.just(InteractionResponse.builder()
        .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .content(RAID_STATS_EXPLANATION)
            .flags(MessageUtil.EPHEMERAL_BYTE)
            .build())
        .build());
  }
}
