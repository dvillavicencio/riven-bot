package com.deahtstroke.rivenbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerEntryDetails {

  /**
   * The name of the player participant
   */
  private String playerName;

  /**
   * The tag of the player
   */
  private Integer playerTag;

  /**
   * The icon of the player at the time
   */
  private String playerIcon;

  /**
   * The player membership type
   */
  private Integer membershipType;

  /**
   * The player membership ID
   */
  private Long membershipId;

  /**
   * Total amount of deaths
   */
  private Integer deaths;

  /**
   * Total amount of kills
   */
  private Integer kills;
}
