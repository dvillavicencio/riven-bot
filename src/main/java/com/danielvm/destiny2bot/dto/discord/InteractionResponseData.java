package com.danielvm.destiny2bot.dto.discord;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionResponseData {

  /**
   * The message content of the InteractionResponse
   */
  private String content;

  /**
   * List of embeds{@link }
   */
  private List<Embedded> embeds;

  /**
   * List of choices for autocomplete interactions
   */
  private List<Choice> choices;

  /**
   * List of components in the message
   */
  private List<Component> components;

  /**
   * Whether this message should be secret or not (disappears after a time)
   */
  private Integer flags;
}
