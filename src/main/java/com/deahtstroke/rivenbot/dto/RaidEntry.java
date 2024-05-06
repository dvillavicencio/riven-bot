package com.deahtstroke.rivenbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidEntry {

  /**
   * The name of the raid
   */
  private String raidName;

  /**
   * The instance ID of the activity in the PGCR
   */
  private Long instanceId;

  /**
   * Total amount of deaths the player had in this activity
   */
  private Integer totalDeaths;

  /**
   * Total amount of kills the player had in this activity
   */
  private Integer totalKills;

  /**
   * The overall KDA the player had in this activity
   */
  private Double kda;

  /**
   * The duration in seconds of this activity
   */
  private Integer duration;

  /**
   * If the player completed this activity
   */
  private Boolean isCompleted;

  /**
   * If the activity was started from the beginning
   */
  private Boolean isFromBeginning;
}
