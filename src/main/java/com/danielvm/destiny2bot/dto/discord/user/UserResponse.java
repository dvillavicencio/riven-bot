package com.danielvm.destiny2bot.dto.discord.user;

import lombok.Data;

@Data
public class UserResponse {

  private String id;

  private String username;

  private String avatar;

  private String locale;
}
