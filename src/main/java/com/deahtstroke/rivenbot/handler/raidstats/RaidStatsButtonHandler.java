package com.deahtstroke.rivenbot.handler.raidstats;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.MessageComponentId;
import com.deahtstroke.rivenbot.handler.MessageComponentHandler;
import com.deahtstroke.rivenbot.util.MessageUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RaidStatsButtonHandler implements MessageComponentHandler {

  private static final String RAID_STATS_EXPLANATION = """
      These aggregated raid statistics represent some crunched numbers Bungie has put out for you \
      such as total kills and total deaths in a raid. All the stats you are seeing right now are \
      an aggregated total throughout **all your characters for a single raid**.
            
      Please note that the ":first_place: fastest" stat as of now is calculated only on full raid clears. \
      That means raids that did not start from the beginning **do not count**.""";

  @Override
  public MessageComponentId getComponentId() {
    return MessageComponentId.RAID_STATS_COMPREHENSION;
  }

  @Override
  public Mono<InteractionResponse> serve(Interaction interaction) {
    return Mono.just(InteractionResponse.builder()
        .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .content(RAID_STATS_EXPLANATION)
            .flags(MessageUtils.EPHEMERAL_BYTE)
            .build())
        .build());
  }
}
