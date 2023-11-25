package com.danielvm.destiny2bot.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class TokenResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 1598364837933949635L;

  @JsonAlias("access_token")
  private String accessToken;

  @JsonAlias("token_type")
  private String tokenType;

  @JsonAlias("expires_in")
  private Long expiresIn;

  @JsonAlias("refresh_token")
  private String refreshToken;
}
