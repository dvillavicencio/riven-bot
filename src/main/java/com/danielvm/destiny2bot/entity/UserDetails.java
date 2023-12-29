package com.danielvm.destiny2bot.entity;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.ExplicitEncrypted;

@Data
@Builder
@Document
public class UserDetails {

  @Id
  @ExplicitEncrypted
  private String discordId;


  private String discordUsername;

  @ExplicitEncrypted
  private String accessToken;

  @ExplicitEncrypted
  private String refreshToken;

  @ExplicitEncrypted
  private Instant expiration;
}
