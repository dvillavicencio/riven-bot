package com.danielvm.destiny2bot.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidEncounter {

  /**
   * The name of the encounter
   */
  private String encounterName;

  /**
   * Brief description of the encounter
   */
  private String description;

}
