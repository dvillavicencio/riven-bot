package com.danielvm.destiny2bot.repository;

import com.danielvm.destiny2bot.entity.BotUser;
import com.danielvm.destiny2bot.mapper.BotUserMapper;
import com.danielvm.destiny2bot.mapper.UserCharacterMapper;
import java.util.Map;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class BotUserRepositoryImpl implements BotUserRepository {

  private static final String RETRIEVE_CHARACTERS_QUERY = """
      SELECT buc.character_id,
             buc.light_level,
             buc.destiny_class,
             buc.discord_user_id
      FROM bot_user bu
               INNER JOIN bungie_user_character buc on
                  bu.discord_id = buc.discord_user_id
      WHERE bu.discord_id = :discordId;
            """;

  private static final String BOT_USER_QUERY = """
      SELECT bu.discord_id,
       bu.discord_username,
       bu.bungie_membership_id,
       bu.bungie_access_token,
       bu.bungie_refresh_token,
       bu.bungie_token_expiration
      FROM bot_user bu
      WHERE bu.discord_id = :discordId
      """;

  private static final String INSERT_USER_QUERY = """
      INSERT INTO bot_user (discord_id, discord_username, bungie_membership_id,
       bungie_access_token, bungie_refresh_token, bungie_token_expiration)
        VALUES (:discordId, :discordUsername, :membershipId, :accessToken,
         :refreshToken, :tokenExpiration)
      """;

  private final DatabaseClient databaseClient;
  private final BotUserMapper botUserMapper;
  private final UserCharacterMapper userCharacterMapper;

  public BotUserRepositoryImpl(
      DatabaseClient databaseClient,
      BotUserMapper botUserMapper, UserCharacterMapper userCharacterMapper) {
    this.databaseClient = databaseClient;
    this.botUserMapper = botUserMapper;
    this.userCharacterMapper = userCharacterMapper;
  }

  @Override
  public Mono<BotUser> findBotUserByDiscordId(Long discordId) {
    return databaseClient.sql(BOT_USER_QUERY)
        .bind("discordId", discordId)
        .map(botUserMapper::apply)
        .first().flatMap(botUser -> databaseClient.sql(RETRIEVE_CHARACTERS_QUERY)
            .bind("discordId", discordId)
            .map(userCharacterMapper::apply)
            .all().collectList()
            .map(userCharacters -> {
              botUser.setCharacters(userCharacters);
              return botUser;
            })
        );
  }

  @Override
  public Mono<BotUser> save(BotUser botUser) {
    Map<String, Object> params = Map.of(
        "discordId", botUser.getDiscordId(),
        "discordUsername", botUser.getDiscordUsername(),
        "membershipId", botUser.getBungieMembershipId(),
        "accessToken", botUser.getBungieAccessToken(),
        "refreshToken", botUser.getBungieRefreshToken(),
        "tokenExpiration", botUser.getBungieTokenExpiration()
    );
    return databaseClient.sql(INSERT_USER_QUERY)
        .bindValues(params)
        .map(botUserMapper::apply)
        .one();
  }

}
