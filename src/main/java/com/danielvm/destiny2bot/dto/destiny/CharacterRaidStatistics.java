package com.danielvm.destiny2bot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterRaidStatistics {

  private static final String DISCORD_TEMPLATE = """
      Total Kills: %s
      Total Deaths: %s
      Fastest Time: %s
      Average Time: %s
      Clears: %s
      Full clears: %s
      Uncompleted: %s
      Total Runs: %s""";

  /**
   * The name of the raid
   */
  private String raidName;

  /**
   * Total amount of kills done for a raid
   */
  private Integer totalKills;

  /**
   * Total amount of deaths done in a raid
   */
  private Integer totalDeaths;

  /**
   * The fastest time a player has done in a raid
   */
  private Integer fastestTime;

  /**
   * The average time a player finishes the raid
   */
  private Integer averageTime;

  /**
   * The number of completed raids that user has for a specific raid
   */
  private Integer clears;

  /**
   * The number of incomplete raids the user has for a specific raid
   */
  private Integer uncompleted;

  /**
   * The total amount of times a player has played this raid
   */
  private Integer totalRuns;

  /**
   * The total amount of full clears for a raid
   */
  private Integer fullClears;

  public CharacterRaidStatistics(String raidName) {
    this.raidName = raidName;
    this.totalKills = 0;
    this.totalDeaths = 0;
    this.fastestTime = Integer.MAX_VALUE;
    this.averageTime = 0;
    this.clears = 0;
    this.uncompleted = 0;
    this.totalRuns = 0;
    this.fullClears = 0;
  }

  public String toString() {
    return DISCORD_TEMPLATE.formatted(
        this.totalKills,
        this.totalDeaths,
        this.fastestTime,
        this.averageTime,
        this.clears,
        this.fullClears,
        this.uncompleted,
        this.totalRuns
    );
  }
}
