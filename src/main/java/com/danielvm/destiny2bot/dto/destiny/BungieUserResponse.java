package com.danielvm.destiny2bot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BungieUserResponse {

  private String membershipId;

  private String uniqueName;

  private Boolean isDeleted;

  private String locale;
}
