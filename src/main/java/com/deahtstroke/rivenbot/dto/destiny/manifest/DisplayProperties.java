package com.deahtstroke.rivenbot.dto.destiny.manifest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayProperties {

  private String description;
  private String name;
  private String icon;
  private String highResIcon;
  private Boolean hasIcon;
}
