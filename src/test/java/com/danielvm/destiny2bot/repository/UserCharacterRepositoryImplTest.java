package com.danielvm.destiny2bot.repository;

import static com.danielvm.destiny2bot.repository.UserCharacterRepositoryImpl.INSERT_CHARACTER_QUERY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.entity.UserCharacter;
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

@ExtendWith(MockitoExtension.class)
public class UserCharacterRepositoryImplTest {

  @Mock
  private DatabaseClient databaseClient;

  @InjectMocks
  private UserCharacterRepositoryImpl sut;

  @Test
  @DisplayName("Save user character is successful")
  public void saveUserCharacterIsSuccessful() {
    // given: a user character to save
    UserCharacter userCharacter = new UserCharacter(
        1L, 1801, "Titan", 12345L);

    Map<String, Object> parameters = Map.of(
        "characterId", userCharacter.getCharacterId(),
        "lightLevel", userCharacter.getLightLevel(),
        "discordUserId", userCharacter.getDiscordUserId(),
        "destinyClass", userCharacter.getDestinyClass()
    );

    GenericExecuteSpec genericExecuteSpec = mock(GenericExecuteSpec.class);
    RowsFetchSpec<UserCharacter> rowsFetchSpec = mock(RowsFetchSpec.class);

    when(databaseClient.sql(INSERT_CHARACTER_QUERY)).thenReturn(genericExecuteSpec);
    when(genericExecuteSpec.bindValues(parameters)).thenReturn(genericExecuteSpec);
    when(genericExecuteSpec.map(any(BiFunction.class))).thenReturn(rowsFetchSpec);
    when(rowsFetchSpec.one()).thenReturn(Mono.just(userCharacter));

    // when: save is called for a user character
    var result = StepVerifier.create(sut.save(userCharacter));

    // then: the saved character is returned
    result.assertNext(user -> {
      assertThat(user.getDiscordUserId()).isEqualTo(userCharacter.getDiscordUserId());
      assertThat(user.getCharacterId()).isEqualTo(userCharacter.getCharacterId());
      assertThat(user.getLightLevel()).isEqualTo(userCharacter.getLightLevel());
      assertThat(user.getDestinyClass()).isEqualTo(userCharacter.getDestinyClass());
    }).verifyComplete();
  }

}
