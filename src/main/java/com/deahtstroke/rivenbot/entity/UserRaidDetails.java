package com.deahtstroke.rivenbot.entity;

import com.deahtstroke.rivenbot.enums.RaidDifficulty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRaidDetails implements Serializable {

  /**
   * The name of the raid
   */
  private String raidName;

  /**
   * The difficulty of the raid, e.g., NORMAL or MASTER
   */
  private RaidDifficulty raidDifficulty;

  /**
   * The size of the fireteam that completed the raid
   */
  private Integer completionFireteamSize;

  /**
   * Whether the user completed this raid or not
   */
  private Boolean isCompleted;

  /**
   * Total number of kills in the raid
   */
  private Integer totalKills;

  /**
   * Total number of deaths in the raid
   */
  private Integer totalDeaths;

  /**
   * The KDA of the player in the raid
   */
  private Double kda;

  /**
   * Duration of the raid in seconds
   */
  private Integer durationSeconds;

  /**
   * If the raid was started from the beginning
   */
  private Boolean fromBeginning;

  /**
   * The related PGCR ID that relates to this specific instance of the raid. This field is more for
   * reference purposes, so we can always reference back to the PGCR in case we need more
   * information.
   */
  private Long instanceId;
}
