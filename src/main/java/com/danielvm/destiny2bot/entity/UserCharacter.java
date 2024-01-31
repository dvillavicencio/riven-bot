package com.danielvm.destiny2bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCharacter {

  private Long characterId;

  private Integer lightLevel;

  private String destinyClass;

  private Long discordUserId;
}
