package com.danielvm.destiny2bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidParticipant {

  private Long membershipId;

  private String username;

  private String characterClass;

  private String iconPath;

  private Boolean completed;

  private Long raidInstance;
}
