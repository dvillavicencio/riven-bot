package com.danielvm.destiny2bot.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotUser {

  private Long discordId;

  private String discordUsername;

  private Long bungieMembershipId;

  private String bungieAccessToken;

  private String bungieRefreshToken;

  private Long bungieTokenExpiration;

  private List<UserCharacter> characters;
}
