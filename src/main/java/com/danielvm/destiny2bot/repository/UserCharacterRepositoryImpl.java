package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.UserCharacter;
import com.danielvm.destiny2bot.mapper.UserCharacterMapper;
import java.util.Map;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserCharacterRepositoryImpl implements UserCharacterRepository {

  private static final UserCharacterMapper USER_CHARACTER_MAPPER = new UserCharacterMapper();
  public static final String INSERT_CHARACTER_QUERY = """
      INSERT INTO bungie_user_character (character_id, light_level, destiny_class, discord_user_id)
      VALUES (:characterId, :lightLevel, :destinyClass, :discordUserId)""";

  private final DatabaseClient databaseClient;

  public UserCharacterRepositoryImpl(
      DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<UserCharacter> save(UserCharacter userCharacter) {
    Map<String, Object> saveParameters = Map.of(
        "characterId", userCharacter.getCharacterId(),
        "lightLevel", userCharacter.getLightLevel(),
        "destinyClass", userCharacter.getDestinyClass(),
        "discordUserId", userCharacter.getDiscordUserId()
    );
    return databaseClient.sql(INSERT_CHARACTER_QUERY)
        .bindValues(saveParameters)
        .map(USER_CHARACTER_MAPPER::apply)
        .one();
  }
}
