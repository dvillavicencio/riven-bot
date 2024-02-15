package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidEntry {

  private String raidName;

  private Long instanceId;

  private Integer totalDeaths;

  private Integer totalKills;

  private Double kda;

  private Integer duration;

  private Boolean isCompleted;

  private Boolean isFromBeginning;
}
