package com.deahtstroke.rivenbot.dto.discord;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponseData implements Serializable {

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
  private List<MessageComponent> components;

  /**
   * List of partial attachments to send
   */
  private List<Attachment> attachments;

  /**
   * Whether this message should be secret or not (disappears after a time)
   */
  private Integer flags;
}
