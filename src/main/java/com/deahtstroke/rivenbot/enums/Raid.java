package com.deahtstroke.rivenbot.enums;

import com.deahtstroke.rivenbot.dto.SocialLink;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;

public enum Raid {
  LAST_WISH("Last Wish", "last_wish",
      "a-phantom-moon",
      List.of(
          new SocialLink(SocialPlatform.DEVIANTART, "https://www.deviantart.com/a-phantom-moon"))),
  GARDEN_OF_SALVATION("Garden of Salvation", "garden_of_salvation", null, null),
  DEEP_STONE_CRYPT("Deep Stone Crypt", "deep_stone_crypt", null, null),
  VAULT_OF_GLASS("Vault of Glass", "vault_of_glass", "SCA",
      List.of(
          new SocialLink(SocialPlatform.STEAM, "https://steamcommunity.com/id/scaro25"))),
  VOW_OF_THE_DISCIPLE("Vow of the Disciple", "vow_of_the_disciple", null, null),
  KINGS_FALL("King's Fall", "kings_fall", null, null),
  ROOT_OF_NIGHTMARES("Root of Nightmares", "root_of_nightmares", null, null),
  CROTAS_END("Crota's End", "crotas_end", null, null);

  @Getter
  private final String raidName;

  @Getter
  private final String raidDirectory;

  @Getter
  private final String artistName;

  @Getter
  private final List<SocialLink> artistSocials;

  Raid(String raidName, String raidDirectory, String artistName, List<SocialLink> artistSocials) {
    this.raidName = raidName;
    this.raidDirectory = raidDirectory;
    this.artistName = artistName;
    this.artistSocials = artistSocials;
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
