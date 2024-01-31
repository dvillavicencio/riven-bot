package com.danielvm.destiny2bot.repository;

import static com.danielvm.destiny2bot.repository.BotUserRepositoryImpl.INSERT_USER_QUERY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.entity.BotUser;
import com.danielvm.destiny2bot.entity.UserCharacter;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import com.danielvm.destiny2bot.mapper.BotUserMapper;
import java.util.List;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
public class BotUserRepositoryImplTest {

  @Mock
  DatabaseClient databaseClient;

  @InjectMocks
  BotUserRepositoryImpl sut;

  @Test
  @DisplayName("Retrieving Bot user w/characters is successful")
  public void retrieveBotUserTest() {
    // given: a bot user to save to the DB
    BotUser user = new BotUser(1234L, "Deahtstroke", 56789L,
        "someAccessToken", "someRefreshToken", 3600L, null);
    BotUserMapper mapper = new BotUserMapper();

    Map<String, Object> params = Map.of(
        "discordId", user.getDiscordId(),
        "discordUsername", user.getDiscordUsername(),
        "membershipId", user.getBungieMembershipId(),
        "accessToken", user.getBungieAccessToken(),
        "refreshToken", user.getBungieRefreshToken(),
        "tokenExpiration", user.getBungieTokenExpiration()
    );

    RowsFetchSpec<BotUser> botUserRowsFetchSpec = mock(RowsFetchSpec.class);
    GenericExecuteSpec genericExecuteSpec = mock(GenericExecuteSpec.class);
    when(databaseClient.sql(INSERT_USER_QUERY))
        .thenReturn(genericExecuteSpec);
    when(genericExecuteSpec.bindValues(params))
        .thenReturn(genericExecuteSpec);

    when(genericExecuteSpec.map(any(BiFunction.class)))
        .thenReturn(botUserRowsFetchSpec);

    when(botUserRowsFetchSpec.one())
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

  @Test
  @DisplayName("findByDiscordId is successful")
  public void findByDiscordIdIsSuccessful() {
    // given: some DiscordId
    Long discordId = 173312L;

    List<UserCharacter> characters = List.of(
        new UserCharacter(1L, 1810, "Titan", 12L),
        new UserCharacter(2L, 1789, "Hunter", 12L)
    );
    BotUser user = new BotUser(12L, "deaht", 34L,
        "someAccessToken", "someRefreshToken", 3600L, null);

    GenericExecuteSpec botUserGenericSpec = mock(GenericExecuteSpec.class);
    RowsFetchSpec<BotUser> botUserRowsFetchSpec = mock(RowsFetchSpec.class);
    GenericExecuteSpec userCharacterGenericSpec = mock(GenericExecuteSpec.class);
    RowsFetchSpec<UserCharacter> userCharacterRowsFetchSpec = mock(RowsFetchSpec.class);
    when(databaseClient.sql("""
        SELECT bu.discord_id,
         bu.discord_username,
         bu.bungie_membership_id,
         bu.bungie_access_token,
         bu.bungie_refresh_token,
         bu.bungie_token_expiration
        FROM bot_user bu
        WHERE bu.discord_id = :discordId
        """)).thenReturn(botUserGenericSpec);
    when(botUserGenericSpec.bind("discordId", discordId)).thenReturn(botUserGenericSpec);
    when(botUserGenericSpec.map(any(BiFunction.class))).thenReturn(botUserRowsFetchSpec);
    when(botUserRowsFetchSpec.first()).thenReturn(Mono.just(user));

    when(databaseClient.sql("""
        SELECT buc.character_id,
               buc.light_level,
               buc.destiny_class,
               buc.discord_user_id
        FROM bot_user bu
                 INNER JOIN bungie_user_character buc on
                    bu.discord_id = buc.discord_user_id
        WHERE bu.discord_id = :discordId
        """)).thenReturn(userCharacterGenericSpec);
    when(userCharacterGenericSpec.bind("discordId", discordId)).thenReturn(
        userCharacterGenericSpec);
    when(userCharacterGenericSpec.map(any(BiFunction.class))).thenReturn(
        userCharacterRowsFetchSpec);
    when(userCharacterRowsFetchSpec.all()).thenReturn(Flux.fromIterable(characters));

    // when: findByDiscordId is called
    var result = StepVerifier.create(sut.findBotUserByDiscordId(discordId));

    // then: the found botUser has the correct fields
    result.assertNext(botUser -> {
      assertThat(botUser.getBungieTokenExpiration()).isEqualTo(user.getBungieTokenExpiration());
      assertThat(botUser.getBungieMembershipId()).isEqualTo(user.getBungieMembershipId());
      assertThat(botUser.getBungieRefreshToken()).isEqualTo(user.getBungieRefreshToken());
      assertThat(botUser.getBungieAccessToken()).isEqualTo(user.getBungieAccessToken());
      assertThat(botUser.getDiscordUsername()).isEqualTo(user.getDiscordUsername());
      assertThat(botUser.getDiscordId()).isEqualTo(user.getDiscordId());
      botUser.getCharacters().forEach(character -> {
        if (character.getCharacterId().equals(characters.get(0).getCharacterId())) {
          assertThat(character.getDiscordUserId()).isEqualTo(characters.get(0).getDiscordUserId());
          assertThat(character.getDestinyClass()).isEqualTo(characters.get(0).getDestinyClass());
          assertThat(character.getLightLevel()).isEqualTo(characters.get(0).getLightLevel());
        } else {
          assertThat(character.getDiscordUserId()).isEqualTo(characters.get(1).getDiscordUserId());
          assertThat(character.getDestinyClass()).isEqualTo(characters.get(1).getDestinyClass());
          assertThat(character.getLightLevel()).isEqualTo(characters.get(1).getLightLevel());
        }
      });
    }).verifyComplete();
  }

  @Test
  @DisplayName("findByDiscordId throws exception when Bot User is not found")
  public void findByDiscordIdShouldThrowException() {
    // given: some DiscordId
    Long discordId = 173312L;

    GenericExecuteSpec botUserGenericSpec = mock(GenericExecuteSpec.class);
    RowsFetchSpec<BotUser> botUserRowsFetchSpec = mock(RowsFetchSpec.class);
    when(databaseClient.sql("""
        SELECT bu.discord_id,
         bu.discord_username,
         bu.bungie_membership_id,
         bu.bungie_access_token,
         bu.bungie_refresh_token,
         bu.bungie_token_expiration
        FROM bot_user bu
        WHERE bu.discord_id = :discordId
        """)).thenReturn(botUserGenericSpec);
    when(botUserGenericSpec.bind("discordId", discordId)).thenReturn(botUserGenericSpec);
    when(botUserGenericSpec.map(any(BiFunction.class))).thenReturn(botUserRowsFetchSpec);
    when(botUserRowsFetchSpec.first()).thenReturn(Mono.empty());

    // when: findByDiscordId is called
    // then: a ResourceNotFoundException is thrown as a result of an empty Mono
    StepVerifier.create(sut.findBotUserByDiscordId(discordId))
        .expectError(ResourceNotFoundException.class)
        .verify();

    // and: the appropriate error message is thrown with the exception
    StepVerifier.create(sut.findBotUserByDiscordId(discordId))
        .expectErrorMessage("Discord user with Id [%s] not found".formatted(discordId))
        .verify();

  }

}
