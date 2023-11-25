package com.danielvm.destiny2bot.dto.discord.interactions;

import lombok.Data;

@Data
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
