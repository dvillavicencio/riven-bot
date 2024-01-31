package com.danielvm.destiny2bot.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.entity.BotUser;
import io.r2dbc.spi.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BotUserMapperTest {

  private final BotUserMapper sut = new BotUserMapper();

  @Test
  @DisplayName("Mapping an SQL row to BotUser is successful")
  public void mappingRowToBotUserIsSuccessful() {
    // given: a Table Row from an SQL query
    Row rowMock = mock(Row.class);
    when(rowMock.get("discord_id", Long.class)).thenReturn(172731L);
    when(rowMock.get("discord_username", String.class)).thenReturn("Deahtstroke");
    when(rowMock.get("bungie_membership_id", Long.class)).thenReturn(28134L);
    when(rowMock.get("bungie_access_token", String.class)).thenReturn("someAccessToken");
    when(rowMock.get("bungie_refresh_token", String.class)).thenReturn("someRefreshToken");
    when(rowMock.get("bungie_token_expiration", Long.class)).thenReturn(3600L);

    // when: applying the BiFunction
    BotUser result = sut.apply(rowMock, null);

    // then: the resulting BotUser object has correct fields
    assertThat(result.getDiscordId()).isEqualTo(172731L);
    assertThat(result.getDiscordUsername()).isEqualTo("Deahtstroke");
    assertThat(result.getBungieMembershipId()).isEqualTo(28134L);
    assertThat(result.getBungieAccessToken()).isEqualTo("someAccessToken");
    assertThat(result.getBungieRefreshToken()).isEqualTo("someRefreshToken");
    assertThat(result.getBungieTokenExpiration()).isEqualTo(3600L);
    assertThat(result.getCharacters()).isEmpty();
  }

}
