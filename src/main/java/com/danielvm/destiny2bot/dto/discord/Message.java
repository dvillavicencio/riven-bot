package com.danielvm.destiny2bot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

  /**
   * ID of the message
   */
  private Long id;

  /**
   * ID of the channel where the message was sent
   */
  private Long channelId;
}
