package com.danielvm.destiny2bot.entity;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Builder
public class UserDetails {

  @Id
  private String discordId;

  private String discordUsername;

  private String accessToken;

  private String refreshToken;

  private Instant expiration;
}
