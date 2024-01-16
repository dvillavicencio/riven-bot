package com.danielvm.destiny2bot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Emoji {

  /**
   * Emoji Id
   */
  private Object id;

  /**
   * Name of the emoji
   */
  private String name;
}
