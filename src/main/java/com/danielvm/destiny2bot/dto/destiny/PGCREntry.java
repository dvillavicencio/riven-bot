package com.danielvm.destiny2bot.dto.destiny;

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
