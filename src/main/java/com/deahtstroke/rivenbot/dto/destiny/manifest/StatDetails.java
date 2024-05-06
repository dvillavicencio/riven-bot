package com.deahtstroke.rivenbot.dto.destiny.manifest;

import lombok.Data;

@Data
public class StatDetails {

  private Long statHash;
  private Integer value;
  private Integer minimum;
  private Integer maximum;
  private Integer displayMaximum;
}
