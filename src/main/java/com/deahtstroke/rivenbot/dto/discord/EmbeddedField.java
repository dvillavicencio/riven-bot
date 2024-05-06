package com.deahtstroke.rivenbot.dto.discord;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmbeddedField {

  private String name;

  private String value;

  private Boolean inline;
}
