package com.deahtstroke.rivenbot.dto.destiny.characters;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Characters {

  private Map<String, UserCharacter> data;
}
