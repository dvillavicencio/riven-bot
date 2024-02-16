package com.danielvm.destiny2bot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidStatistics {

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
  private Integer partialClears;

  /**
   * The total amount of times a player has played this raid
   */
  private Integer totalClears;

  /**
   * The total amount of full clears for a raid
   */
  private Integer fullClears;

  public RaidStatistics(String raidName) {
    this.raidName = raidName;
    this.totalKills = 0;
    this.totalDeaths = 0;
    this.fastestTime = Integer.MAX_VALUE;
    this.averageTime = 0;
    this.partialClears = 0;
    this.totalClears = 0;
    this.fullClears = 0;
  }

  public String toString() {
    StringBuilder fastestRaidDuration = new StringBuilder();
    int hours = (fastestTime / 3600) % 24;
    int minutes = (fastestTime / 60) % 60;
    if (hours > 0) {
      fastestRaidDuration.append(hours).append("hr(s)").append(" ");
    }
    if (minutes > 0) {
      fastestRaidDuration.append(minutes).append("mins");
    }
    StringBuilder raidTemplate = new StringBuilder();
    raidTemplate.append(":crossed_swords: ").append("Kills: ").append(this.totalKills)
        .append("\n");
    raidTemplate.append(":skull_crossbones: ").append("Deaths: ").append(this.totalDeaths)
        .append("\n");
    if (!fastestRaidDuration.isEmpty()) {
      raidTemplate.append(":first_place: ").append("Fastest: ").append(fastestRaidDuration)
          .append("\n");
    }
    raidTemplate.append(":bar_chart: ").append("Total Clears: ").append(this.totalClears)
        .append("\n");
    raidTemplate.append(":trophy: ").append("Full Clears: ").append(this.fullClears)
        .append("\n");
    return raidTemplate.toString();
  }
}
