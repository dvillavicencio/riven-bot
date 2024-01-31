package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.entity.BotUser;
import io.r2dbc.spi.Row;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class BotUserMapper implements BiFunction<Row, Object, BotUser> {

  @Override
  public BotUser apply(Row row, Object o) {
    Long discordId = row.get("discord_id", Long.class);
    String discordUsername = row.get("discord_username", String.class);
    Long membershipId = row.get("bungie_membership_id", Long.class);
    String accessToken = row.get("bungie_access_token", String.class);
    String refreshToken = row.get("bungie_refresh_token", String.class);
    Long tokenExpiration = row.get("bungie_token_expiration", Long.class);
    return new BotUser(discordId, discordUsername, membershipId, accessToken, refreshToken,
        tokenExpiration, new ArrayList<>());
  }
}
