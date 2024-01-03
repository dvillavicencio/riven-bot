package com.danielvm.destiny2bot.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetails implements Serializable {

  @Serial
  private static final long serialVersionUID = 6161559188488304844L;

  private String discordId;

  private String discordUsername;

  private String accessToken;

  private String refreshToken;

  private Instant expiration;
}
