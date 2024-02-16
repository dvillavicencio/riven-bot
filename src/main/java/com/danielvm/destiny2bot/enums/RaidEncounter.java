package com.danielvm.destiny2bot.enums;

import lombok.Getter;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum RaidEncounter {

  // Root of Nightmares
  CATACLYSM(Raid.ROOT_OF_NIGHTMARES, "Cataclysm", "cataclysm"),
  SCISSION(Raid.ROOT_OF_NIGHTMARES, "Scission", "scission"),
  CHASM(Raid.ROOT_OF_NIGHTMARES, "Chasm", "chasm"),
  ZO_AURC(Raid.ROOT_OF_NIGHTMARES, "Zo'Aurc, Explicator of Planets", "zoaurc"),
  NEZAREC(Raid.ROOT_OF_NIGHTMARES, "Nezarec, the Final God of Pain", "nezarec"),

  // Last Wish
  KALLI(Raid.LAST_WISH, "Kalli, the Corrupted", "kalli"),
  SHURO_CHI(Raid.LAST_WISH, "Shuro Chi, the Corrupted", "shuro_chi"),
  MORGETH(Raid.LAST_WISH, "Morgeth, the Spirekeeper", "morgeth"),
  VAULT_SECURITY_MECHANISM(Raid.LAST_WISH, "Vault Security Mechanism", "vault_security"),
  RIVEN_OF_A_THOUSAND_VOICES(Raid.LAST_WISH, "Riven of a Thousand Voices", "riven"),
  QUEENSWALK(Raid.LAST_WISH, "Queenswalk", "queenswalk"),

  // Garden of Salvation
  EMBRACE(Raid.GARDEN_OF_SALVATION, "Embrace", "embrace"),
  UNDERGROWTH(Raid.GARDEN_OF_SALVATION, "Undergrowth", "undergrowth"),
  CONSECRATED_MIND(Raid.GARDEN_OF_SALVATION, "Consecrated Mind, Sol Inherent", "consecrated_mind"),
  SANCTIFIED_MIND(Raid.GARDEN_OF_SALVATION, "Sanctified Mind, Sol Inherent", "sanctified_mind"),

  // Deep Stone Crypt
  PIKE_AND_SPARROW(Raid.DEEP_STONE_CRYPT, "Entrance: Pyke & Sparrow", "entrance"),
  CRYPT_SECURITY(Raid.DEEP_STONE_CRYPT, "Crypt Security", "crypt_security"),
  ATRAKS_1(Raid.DEEP_STONE_CRYPT, "Atraks-1", "atraks_1"),
  TANIKS_REBORN(Raid.DEEP_STONE_CRYPT, "Taniks-Reborn/Nuclear Descent", "taniks_reborn"),
  TANIKS_THE_ABOMNITAION(Raid.DEEP_STONE_CRYPT, "Taniks, the Abomination", "taniks_the_abomination"),

  // Vault of Glass
  OPEN_THE_VAULT(Raid.VAULT_OF_GLASS, "Opening the Vault", "the_vault"),
  CONFLUXES(Raid.VAULT_OF_GLASS, "Confluxes", "confluxes"),
  ORACLES(Raid.VAULT_OF_GLASS, "Oracles", "oracles"),
  THE_TEMPLAR(Raid.VAULT_OF_GLASS, "The Templar", "templar"),
  GORGONS_LABRYNTH(Raid.VAULT_OF_GLASS, "Gorgon's Labrynth", "gorgons_labrynth"),
  GATEKEEPER(Raid.VAULT_OF_GLASS, "The Gatekeeper", "gatekeeper"),
  ATHEON(Raid.VAULT_OF_GLASS, "Atheon, Time's Conflux", "atheon"),

  // Vow of the Disciple
  ACQUISITION(Raid.VOW_OF_THE_DISCIPLE, "Acquisition", "acquisition"),
  CARETAKER(Raid.VOW_OF_THE_DISCIPLE, "The Caretaker", "caretaker"),
  EXHIBITION(Raid.VOW_OF_THE_DISCIPLE, "Exhibition", "exhibition"),
  RHULK(Raid.VOW_OF_THE_DISCIPLE, "Rhulk, Disciple of the Witness", "rhulk"),

  // King's Fall
  PORTAL(Raid.KINGS_FALL, "Open the Portal", "portal"),
  TOMB_SHIPS(Raid.KINGS_FALL, "Tombships", "tombships"),
  ANNIHILATOR_TOTEMS(Raid.KINGS_FALL, "Annihilator Totems", "totems"),
  WARPRIEST(Raid.KINGS_FALL, "Warpriest", "warpriest"),
  GOLGOROTHS_MAZE(Raid.KINGS_FALL, "Golgoroth's Maze", "golgoroth_maze"),
  GOLGOROTH(Raid.KINGS_FALL, "Golgoroth", "golgoroth"),
  DICK_WALL(Raid.KINGS_FALL, "Dick Wall", "dick_wall"),
  DAUGHTERS(Raid.KINGS_FALL, "Daughter's of Oryx", "daughters"),
  ORYX(Raid.KINGS_FALL, "Oryx, the Taken King", "oryx"),

  // Crota's
  STILLS(Raid.CROTAS_END, "The Stills", "stills"),
  BRIDGE(Raid.CROTAS_END, "The Bridge", "bridge"),
  IR_YUT(Raid.CROTAS_END, "Ir Yut, the Deathsinger", "ir_yut"),
  CROTA(Raid.CROTAS_END, "Crota, Son of Oryx", "crota");

  @Getter
  private final String name;
  @Getter
  private final String directory;
  @Getter
  private final Raid raid;

  RaidEncounter(Raid raid, String name, String directory) {
    this.raid = raid;
    this.name = name;
    this.directory = directory;
  }

  /**
   * Get all the raid encounters for a given raid wrapped in a Flux
   *
   * @param raid The raid
   * @return Flux of {@link RaidEncounter}s
   */
  public static Flux<RaidEncounter> getRaidEncounters(Raid raid) {
    List<RaidEncounter> filteredResults = Arrays.stream(RaidEncounter.values())
        .filter(raidEncounter -> Objects.equals(raidEncounter.getRaid(), raid))
        .toList();
    return Flux.fromIterable(filteredResults);
  }

  /**
   * Get the human-readable encounter name based on a directory name
   *
   * @param raid      The Raid
   * @param parameter The encounter directory parameter to find (snake-case)
   * @return The encounter name
   */
  public static RaidEncounter findEncounter(Raid raid, String parameter) {
    return Arrays.stream(RaidEncounter.values())
        .filter(encounter -> Objects.equals(encounter.getRaid(), raid))
        .filter(encounter ->
            Objects.equals(encounter.getName(), parameter) ||
            Objects.equals(encounter.getDirectory(), parameter))
        .findFirst().orElseThrow();
  }
}
