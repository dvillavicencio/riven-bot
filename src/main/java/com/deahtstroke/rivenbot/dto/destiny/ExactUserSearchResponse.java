package com.deahtstroke.rivenbot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExactUserSearchResponse {

  private String bungieGlobalDisplayName;

  private Integer bungieGlobalDisplayNameCode;

  private Integer membershipType;

  private String membershipId;

  private String displayName;
}
