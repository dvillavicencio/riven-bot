package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.entity.UserCharacter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;

@Component
public class UserCharacterMapper implements BiFunction<Row, Object, UserCharacter> {

  @Override
  public UserCharacter apply(Row row, Object o) {
    Long characterId = row.get("character_id", Long.class);
    Integer lightLevel = row.get("light_level", Integer.class);
    String destinyClass = row.get("destiny_class", String.class);
    Long discordUserId = row.get("discord_user_id", Long.class);
    return new UserCharacter(characterId, lightLevel, destinyClass, discordUserId);
  }
}
