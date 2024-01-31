package com.danielvm.destiny2bot.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.entity.BotUser;
import com.danielvm.destiny2bot.mapper.BotUserMapper;
import io.r2dbc.spi.Row;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
public class BotUserRepositoryImplTest {

  @Mock
  DatabaseClient databaseClient;

  @Mock
  GenericExecuteSpec genericExecuteSpec;

  @Mock
  RowsFetchSpec<BotUser> rowsFetchSpec;

  @Mock
  BotUserMapper botUserMapper;

  @Mock
  Row row;

  @InjectMocks
  BotUserRepositoryImpl sut;

  @Test
  @DisplayName("Retrieving Bot user w/characters is successful")
  public void retrieveBotUserTest() {
    // given: a bot user to save to the DB
    BotUser user = new BotUser(1234L, "Deahtstroke", 56789L,
        "someAccessToken", "someRefreshToken", 3600L, null);

    Map<String, Object> params = Map.of(
        "discordId", user.getDiscordId(),
        "discordUsername", user.getDiscordUsername(),
        "membershipId", user.getBungieMembershipId(),
        "accessToken", user.getBungieAccessToken(),
        "refreshToken", user.getBungieRefreshToken(),
        "tokenExpiration", user.getBungieTokenExpiration()
    );

    when(databaseClient.sql("""
        INSERT INTO bot_user (discord_id, discord_username, bungie_membership_id,
         bungie_access_token, bungie_refresh_token, bungie_token_expiration)
          VALUES (:discordId, :discordUsername, :membershipId, :accessToken,
           :refreshToken, :tokenExpiration)
        """))
        .thenReturn(genericExecuteSpec);

    when(genericExecuteSpec.bindValues(params))
        .thenReturn(genericExecuteSpec);

    when(genericExecuteSpec.map(any(BiFunction.class)))
        .thenReturn(rowsFetchSpec);

    when(rowsFetchSpec.one())
        .thenReturn(Mono.just(user));

    // when: save is called with the user
    FirstStep<BotUser> result = StepVerifier.create(sut.save(user));

    // then: the given user is returned successfully
    result.assertNext(botUser -> {
      assertThat(botUser.getDiscordId()).isEqualTo(user.getDiscordId());
      assertThat(botUser.getBungieAccessToken()).isEqualTo(user.getBungieAccessToken());
      assertThat(botUser.getBungieRefreshToken()).isEqualTo(user.getBungieRefreshToken());
      assertThat(botUser.getBungieTokenExpiration()).isEqualTo(user.getBungieTokenExpiration());
    }).verifyComplete();
  }

}
