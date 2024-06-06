package com.deahtstroke.rivenbot.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.data.util.Pair;

public enum Raid {

  LAST_WISH("Last Wish", "last_wish"),
  GARDEN_OF_SALVATION("Garden of Salvation", "garden_of_salvation"),
  DEEP_STONE_CRYPT("Deep Stone Crypt", "deep_stone_crypt"),
  VAULT_OF_GLASS("Vault of Glass", "vault_of_glass"),
  VOW_OF_THE_DISCIPLE("Vow of the Disciple", "vow_of_the_disciple"),
  KINGS_FALL("King's Fall", "kings_fall"),
  ROOT_OF_NIGHTMARES("Root of Nightmares", "root_of_nightmares"),
  CROTAS_END("Crota's End", "crotas_end");

  @Getter
  private final String raidName;

  @Getter
  private final String raidDirectory;

  @Getter
  private final Map<Raid, List<RaidEncounter>> raidEncountersMap;

  Raid(String raidName, String raidDirectory) {
    this.raidName = raidName;
    this.raidDirectory = raidDirectory;
    this.raidEncountersMap = Stream.of(Raid.values())
        .map(raid -> Pair.of(raid, Arrays.stream(RaidEncounter.values())
            .filter(raidEncounter -> Objects.equals(raidEncounter.getRaid(), raid))
            .toList()))
        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
  }

  /**
   * Method that returns a Raid enum constant based on a parameter
   *
   * @param parameter Either the raid name or the raid directory
   * @return {@link Raid}
   */
  public static Raid findRaid(String parameter) {
    return Stream.of(Raid.values())
        .filter(r -> Objects.equals(r.getRaidDirectory(), parameter) || Objects.equals(r.raidName,
            parameter))
        .findFirst().orElse(null);
  }
}
