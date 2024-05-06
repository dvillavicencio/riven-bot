package com.deahtstroke.rivenbot.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChoiceValue {

  /**
   * MembershipId of the user
   */
  private String membershipId;

  /**
   * MembershipType of the user
   */
  private Integer membershipType;

  /**
   * Bungie display name of the user
   */
  private String bungieDisplayName;

  /**
   * Bungie display tag/code of the user
   */
  private Integer bungieDisplayCode;

  /**
   * Clan name of the user
   */
  @Nullable
  private String clanName;
}
