package com.danielvm.destiny2bot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Option {

  /**
   * Name of the option
   */
  private String name;

  /**
   * The type of the option
   */
  private Integer type;

  /**
   * The value of this option
   */
  private Object value;

  /**
   * If this is true, then the option is the focus of an autocomplete interaction
   */
  private Boolean focused;

}
