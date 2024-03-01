package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.UserChoiceValue;
import com.danielvm.destiny2bot.dto.destiny.RaidStatistics;
import com.danielvm.destiny2bot.service.RaidStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
public class InsertUserController {

  private final RaidStatsService raidStatsService;

  public InsertUserController(
      RaidStatsService raidStatsService) {
    this.raidStatsService = raidStatsService;
  }

  /**
   * Inserts a user manually into the Mongo Database
   *
   * @param userId The userId
   * @return the saved entity
   */
  @PostMapping("/user")
  public Flux<RaidStatistics> insertNewUser(@RequestParam String membershipId,
      @RequestParam Integer membershipType, @RequestParam String userId,
      @RequestParam Integer userTag) {
    UserChoiceValue parsedValues = new UserChoiceValue(membershipId, membershipType, userId,
        userTag, null);
    return raidStatsService.calculateRaidStats(parsedValues);
  }

}
