package com.deahtstroke.rivenbot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityValue {

  private DestinyUserInfo destinyUserInfo;

  private String characterClass;

  private Integer lightLevel;
}
