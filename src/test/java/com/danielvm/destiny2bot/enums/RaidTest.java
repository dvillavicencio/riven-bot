package com.danielvm.destiny2bot.enums;

import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RaidTest {

  @Test
  @DisplayName("Finding a Raid works for the name of the directory")
  public void findRaidWorksForAGivenParameter() {
    // given: a raid directory (directories come in lower-case snake case)
    String raidDirectory = "last_wish";

    // when: find raid is called
    var response = Raid.findRaid(raidDirectory);

    // then: the correct Raid enum object is returned
    Assertions.assertThat(response).matches(raid ->
        Objects.equals(raid, Raid.LAST_WISH) &&
        Objects.equals(raid.getRaidDirectory(), raidDirectory));
  }

  @Test
  @DisplayName("Finding a Raid works for the name of the directory")
  public void findRaidWorksForARaidName() {
    // given: a raid name (correctly qualified)
    String raidName = "Last Wish";

    // when: find raid is called
    var response = Raid.findRaid(raidName);

    // then: the correct Raid enum object is returned
    Assertions.assertThat(response).matches(raid ->
        Objects.equals(raid, Raid.LAST_WISH) &&
        Objects.equals(raid.getRaidName(), raidName));
  }
}
