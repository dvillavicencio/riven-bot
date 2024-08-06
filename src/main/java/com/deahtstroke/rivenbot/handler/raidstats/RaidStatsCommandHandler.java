package com.deahtstroke.rivenbot.handler.raidstats;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.handler.SlashCommandHandler;
import com.deahtstroke.rivenbot.processor.AsyncRaidsProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class RaidStatsCommandHandler implements SlashCommandHandler {

  private static final String HASHTAG = "#";

  private final AsyncRaidsProcessor asyncRaidsProcessor;

  public RaidStatsCommandHandler(AsyncRaidsProcessor asyncRaidsProcessor) {
    this.asyncRaidsProcessor = asyncRaidsProcessor;
  }

  @Override
  public Mono<InteractionResponse> serve(Interaction interaction) {
    return Mono.just(InteractionResponse.builder()
            .type(5)
            .data(new InteractionResponseData())
            .build())
        .publishOn(Schedulers.boundedElastic())
        .doOnSubscribe(subscription -> {
          Object optionValue = interaction.getData().getOptions().get(0).getValue();
          String[] values = ((String) optionValue).split(HASHTAG);
          String username = values[0];
          String userTag = values[1];
          asyncRaidsProcessor.processRaidsAsync(username, userTag, interaction.getToken())
              .subscribe();
        });
  }

  @Override
  public SlashCommand getSlashCommand() {
    return SlashCommand.RAID_STATS;
  }
}
