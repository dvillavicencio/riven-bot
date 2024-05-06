package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Channel {

  /**
   * ID of the channel
   */
  private Long id;

  /**
   * ID of the channel the interaction was sent from
   */
  @JsonProperty("channel_id")
  private Long channelId;
}
