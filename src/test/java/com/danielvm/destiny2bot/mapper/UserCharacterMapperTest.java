package com.danielvm.destiny2bot.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.entity.UserCharacter;
import io.r2dbc.spi.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserCharacterMapperTest {

  private final UserCharacterMapper sut = new UserCharacterMapper();

  @Test
  @DisplayName("Mapping Row to UserCharacter works successfully")
  public void successfulMappingRowToUserCharacter() {
    // given: an SQL row that contains user character data
    Row row = mock(Row.class);
    when(row.get("character_id", Long.class)).thenReturn(892812L);
    when(row.get("light_level", Integer.class)).thenReturn(1810);
    when(row.get("destiny_class", String.class)).thenReturn("Titan");
    when(row.get("discord_user_id", Long.class)).thenReturn(172731L);

    // when: applying the BiFunction
    UserCharacter result = sut.apply(row, null);

    // then: the result has correct mappings
    assertThat(result.getCharacterId()).isEqualTo(892812L);
    assertThat(result.getLightLevel()).isEqualTo(1810);
    assertThat(result.getDestinyClass()).isEqualTo("Titan");
    assertThat(result.getDiscordUserId()).isEqualTo(172731L);
  }

}
