package com.deahtstroke.rivenbot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class DestinyMembershipData {

  private Integer membershipType;

  private String membershipId;
}
