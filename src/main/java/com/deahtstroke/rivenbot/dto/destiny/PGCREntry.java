package com.deahtstroke.rivenbot.dto.destiny;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PGCREntry {

  private Integer standing;

  private PlayerPGCREntry player;

  private Map<String, ValueEntry> values;
}
