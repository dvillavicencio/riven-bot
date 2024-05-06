package com.deahtstroke.rivenbot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscordUser {

  /**
   * The user's Identification
   */
  private String id;

  /**
   * The user's username
   */
  private String username;
}
