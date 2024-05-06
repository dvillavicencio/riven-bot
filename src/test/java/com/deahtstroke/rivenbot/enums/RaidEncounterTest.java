package com.deahtstroke.rivenbot.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RaidEncounterTest {

  @Test
  @DisplayName("Finding raid encounters works successfully")
  public void findingRaidEncounterWorksSuccessfully() {
    // given: a raid and a raid encounter
    Raid raid = Raid.KINGS_FALL;
    String name = "Oryx, the Taken King";

    // when: findEncounter is called
    RaidEncounter result = RaidEncounter.findEncounter(raid, name);

    // then: the Raid encounter is Oryx, the Taken King
    assertThat(result).isEqualTo(RaidEncounter.ORYX);
  }

  @Test
  @DisplayName("Finding raid encounters works for directory name")
  public void findingRaidEncounterWorksForDirectoryNameAsParameter() {
    // given: a raid and a raid encounter
    Raid raid = Raid.KINGS_FALL;
    String directory = "oryx";

    // when: findEncounter is called
    RaidEncounter result = RaidEncounter.findEncounter(raid, directory);

    // then: the Raid encounter is Oryx, the Taken King
    assertThat(result).isEqualTo(RaidEncounter.ORYX);
  }

}
