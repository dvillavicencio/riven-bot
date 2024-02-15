package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.destiny.CharacterRaidStatistics;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.service.RaidStatsService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

  private final RaidStatsService raidStatsService;

  public TestController(RaidStatsService raidStatsService) {
    this.raidStatsService = raidStatsService;
  }

  @GetExchange("/stats")
  public Mono<Map<String, CharacterRaidStatistics>> test() {
    return raidStatsService.retrieveRaidStatsForUser(Interaction.builder()
        .data(InteractionData.builder()
            .options(List.of(new Option("username", 1, "4611686018468622561", null)))
            .build())
        .build());
  }
}
