package com.danielvm.destiny2bot.entity;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterRaid {

  private Long instanceId;

  private Instant raidStartTimestamp;

  private Boolean isFromBeginning;

  private Boolean completed;

  private String raidName;

  private Integer numberOfDeaths;

  private Integer opponentsDefeated;

  private Double kda;

  private Integer raidDuration;

  private Long userCharacterId;

  private List<RaidParticipant> participants;
}
