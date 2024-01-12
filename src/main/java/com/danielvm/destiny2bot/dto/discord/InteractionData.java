package com.danielvm.destiny2bot.dto.discord;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionData {

  /**
   * The Id of the invoked command
   */
  private Object id;

  /**
   * The name of the invoked command
   */
  private String name;

  /**
   * The type of the invoked command
   */
  private Integer type;

  /**
   * List of options sent with the interaction
   */
  private List<Option> options;

}
