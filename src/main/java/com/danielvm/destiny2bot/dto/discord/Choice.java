package com.danielvm.destiny2bot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choice {

  /**
   * Name of the choice
   */
  private String name;

  /**
   * Value of the choice
   */
  private Object value;
}
