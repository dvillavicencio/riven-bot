package com.deahtstroke.rivenbot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Emoji {

  /**
   * Emoji Id
   */
  private Object id;

  /**
   * Name of the emoji
   */
  private String name;

  /**
   * Whether this emoji is animated or not
   */
  private Boolean animated;
}
