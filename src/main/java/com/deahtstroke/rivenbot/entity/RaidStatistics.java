package com.deahtstroke.rivenbot.entity;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidStatistics {

  private static final Set<String> RAIDS_WITH_MASTER_MODE = Set.of(
      "Vault of Glass", "Vow of the Disciple", "King's Fall", "Root of Nightmares", "Crota's End"
  );

  /**
   * The _Id of the MongoDB aggregation should be the name of the raid
   */
  private String _id;

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

  /**
   * The total amount of normal-mode raid clears for this raid
   */
  private Integer masterClears;

  /**
   * The total amount of master-mode raid clears for this raid
   */
  private Integer normalClears;

  /**
   * This toString uses a StringBuilder to manipulate the actual output to send through Discord
   * chat
   *
   * @return String representation of what the user will see in the embed for a given raid
   */
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
    if (RAIDS_WITH_MASTER_MODE.contains(this._id) && normalClears != 0) {
      raidTemplate.append(":regional_indicator_n: ").append("Normal Clears: ")
          .append(this.normalClears).append("\n");
    }
    if (RAIDS_WITH_MASTER_MODE.contains(this._id) && masterClears != 0) {
      raidTemplate.append(":regional_indicator_m: ").append("Master Clears: ")
          .append(this.masterClears).append("\n");
    }
    return raidTemplate.toString();
  }
}
