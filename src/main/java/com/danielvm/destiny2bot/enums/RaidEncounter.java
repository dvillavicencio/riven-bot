package com.danielvm.destiny2bot.enums;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaidEncounter {

  /**
   * In-memory map that contains all the encounters alongside the raid that they correspond to. The
   * value of each entry is a list that comprises the name of the encounter and the file directory
   * where the assets can be found under /resources/static/raids/{raidName}/{encounterName}/..
   */
  private static final Map<Raid, List<RaidEncounter>> ENCOUNTERS_BY_RAID =
      Map.of(
          Raid.ROOT_OF_NIGHTMARES, List.of(
              new RaidEncounter("Cataclysm", "cataclysm"),
              new RaidEncounter("Scission", "scission"),
              new RaidEncounter("Chasm", "cross_the_chasm"),
              new RaidEncounter("Zo’Aurc, Explicator of Planets", "zoaurc_explicator_of_planets"),
              new RaidEncounter("Nezarec, the Final God of Pain", "nezarec_the_final_god_of_pain")),
          Raid.LAST_WISH, List.of(
              new RaidEncounter("Kalli, the Corrupted", "kalli"),
              new RaidEncounter("Shuro Chi, the Corrupted", "shuro_chi"),
              new RaidEncounter("Morgeth, the Spirekeeper", "morgeth"),
              new RaidEncounter("Vault Security Mechanism", "vault_security_mechanism"),
              new RaidEncounter("Riven of a Thousand Voices", "riven_of_a_thousand_voices"),
              new RaidEncounter("Queenswalk", "queenswalk")),
          Raid.GARDEN_OF_SALVATION, List.of(
              new RaidEncounter("Embrace", "embrace"),
              new RaidEncounter("Undergrowth", "undergrowth"),
              new RaidEncounter("Consecrated Mind, Sol Inherent", "consecrated_mind"),
              new RaidEncounter("Sanctified Mind, Sol Inherent", "sanctified_mind")
          ),
          Raid.DEEP_STONE_CRYPT, List.of(
              new RaidEncounter("Entrance: Pike & Sparrow", "entrance"),
              new RaidEncounter("Crypt Security", "crypt_security"),
              new RaidEncounter("Atraks-1", "atraks_1"),
              new RaidEncounter("Taniks-Reborn/Nuclear Descent", "taniks_reborn"),
              new RaidEncounter("Taniks, the Abomination", "taniks_the_abomination")),
          Raid.VAULT_OF_GLASS, List.of(
              new RaidEncounter("Opening the Vault", "open_the_vault"),
              new RaidEncounter("Confluxes", "confluxes"),
              new RaidEncounter("Oracles", "oracles"),
              new RaidEncounter("The Templar", "templar"),
              new RaidEncounter("Gorgon's Labrynth", "gorgons_labrynth"),
              new RaidEncounter("Gatekeeper", "gatekeepr"),
              new RaidEncounter("Atheon, Time's Conflux", "atheon_times_conflux")),
          Raid.VOW_OF_THE_DISCIPLE, List.of(
              new RaidEncounter("Disciple's Bog", "disciples_bog"),
              new RaidEncounter("Acquisition", "acquisition"),
              new RaidEncounter("The Caretaker", "the_caretaker"),
              new RaidEncounter("Exhibition", "exhibition"),
              new RaidEncounter("Rhulk, Disciple of The Witness", "rhulk_disciple_of_the_witness")),
          Raid.KINGS_FALL, List.of(
              new RaidEncounter("Cataclysm", "cataclysm"),
              new RaidEncounter("Scission", "scission"),
              new RaidEncounter("Chasm", "cross_the_chasm"),
              new RaidEncounter("Zo’Aurc, Explicator of Planets", "zoaurc_explicator_of_planets"),
              new RaidEncounter("Nezarec, the Final God of Pain", "nezarec_the_final_god_of_pain")),
          Raid.CROTAS_END, List.of(
              new RaidEncounter("The Stills", "the_stills"),
              new RaidEncounter("The Bridge", "bridge"),
              new RaidEncounter("Ir Yut, the Deathsinger", "ir_yut_the_deathsinger"),
              new RaidEncounter("Crota, Son of Oryx", "crota_son_of_oryx")));

  /**
   * The name of the encounter
   */
  private String encounterName;
  /**
   * Directory name of where to find the image assets to retrieve for a given raid encounter
   */
  private String directoryName;

  /**
   * Get all the raid encounters for a given raid wrapped in a Flux
   *
   * @param raid The raid
   * @return Flux of {@link RaidEncounter}s
   */
  public static Flux<RaidEncounter> getRaidEncounters(Raid raid) {
    return Flux.fromIterable(ENCOUNTERS_BY_RAID.get(raid));
  }

  /**
   * Get the human-readable encounter name based on a directory name
   *
   * @param raid      The Raid
   * @param parameter The encounter directory parameter to find (snake-case)
   * @return The encounter name
   */
  public static String findEncounter(Raid raid, String parameter) {
    return ENCOUNTERS_BY_RAID.get(raid).stream()
        .filter(encounter -> Objects.equals(encounter.getDirectoryName(), parameter))
        .map(RaidEncounter::getEncounterName)
        .findFirst().orElseThrow();
  }
}
