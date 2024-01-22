package com.danielvm.destiny2bot.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RaidEncounterTest {

  @Test
  @DisplayName("Finding raid encounters works successfully")
  public void findingRaidEncounterWorksSuccessfully() {
    // given: a raid and a raid encounter
    Raid raid = Raid.KINGS_FALL;
    String raidEncounter = "Oryx, the Taken King";


    // when:
    RaidEncounter.findEncounter()

    // then:
  }

}
