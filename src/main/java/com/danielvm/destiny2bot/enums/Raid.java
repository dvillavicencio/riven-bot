package com.danielvm.destiny2bot.enums;

import lombok.Getter;
import reactor.core.publisher.Flux;

public enum Raid {
  LAST_WISH(
      Flux.just(
          new RaidEncounter("Kalli", "Defeat Kalli"),
          new RaidEncounter("Shuro Chi", "Defeat Shuro Chi"),
          new RaidEncounter("Morgeth", "Defeat Morgeth, the Spirekeeper"),
          new RaidEncounter("Vault Security Mechanism", "Unlock the way forward"),
          new RaidEncounter("Riven of a Thousand Voices", "Slay Riven"),
          new RaidEncounter("Queenswalk", "Take the stone to the Techeun")),
      "Last Wish"),
  GARDEN_OF_SALVATION(
      Flux.just(
          new RaidEncounter("Embrace (1st encounter)", "Track the Unknown Artifact's Signal"),
          new RaidEncounter("Undergrowth (2nd encounter)", "Draw out the Consecrated Mind"),
          new RaidEncounter("Consecrated Mind, Sol Inherent", "Subdue the Consecrated Mind"),
          new RaidEncounter("Sanctified Mind, Sol Inherent", "Conquer the Sanctified Mind")),
      "Garden of Salvation"),
  DEEP_STONE_CRYPT(
      Flux.just(
          new RaidEncounter("Entrance: Pike & Sparrow", "Locate the Deep Stone Crypt"),
          new RaidEncounter("Crypt Security", "Disable Crypt Security"),
          new RaidEncounter("Atraks-1", "Defeat Atraks-1"),
          new RaidEncounter("Taniks-Reborn/Nuclear Descent", "Prevent Europa's Destruction"),
          new RaidEncounter("Taniks, the Abomination", "Defeat Taniks, the Abomination")),
      "Deep Stone Crypt"),
  VAULT_OF_GLASS(
      Flux.just(
          new RaidEncounter("Opening the Vault", "Raise the Spire"),
          new RaidEncounter("Confluxes", "Defend all three Confluxes"),
          new RaidEncounter("Oracles", "Destroy the oracles"),
          new RaidEncounter("The Templar", "Defeat the Templar"),
          new RaidEncounter("Gorgon's Labrynth", "The Labryinth"),
          new RaidEncounter("Gatekeeper", "Awaken the Glass Throne"),
          new RaidEncounter("Atheon, Time's Conflux", "Destroy Atheon")),
      "Vault of Glass"),
  VOW_OF_THE_DISCIPLE(
      Flux.just(
          new RaidEncounter("Disciple's Bog", "Approach children..."),
          new RaidEncounter("Acquisition", "Truth. Symbolize. Is. Materialize. Everywhere."),
          new RaidEncounter("The Caretaker", "Do Not Disrupt the Caretaker"),
          new RaidEncounter("Exhibition", "Nothing More Than Meaningless Trinkets"),
          new RaidEncounter("Rhulk, Disciple of The Witness", "DROWNDROWNDROWN")
      ),
      "Vow of the Disciple"),
  KINGS_FALL(
      Flux.just(
          new RaidEncounter("The Court of Oryx", "Open the Portal"),
          new RaidEncounter("Tomb Ships", "Cross the Expanse"),
          new RaidEncounter("The Totems (Basilica)", "Power the Glyph"),
          new RaidEncounter("The Warpriest", "Defeat the Warpriest"),
          new RaidEncounter("Golgorth's Maze", "Traverse the Catacombs"),
          new RaidEncounter("Golgoroth", "Defeat Golgoroth"),
          new RaidEncounter("The Dick Wall", "Traverse the Edge (The dick wall)"),
          new RaidEncounter("Daughters of Oryx", "Defeat the Daughters of Oryx"),
          new RaidEncounter("Oryx, the Taken King", "Defeat Oryx, the Taken King")
      ),
      "King's Fall"),
  ROOT_OF_NIGHTMARES(
      Flux.just(
          new RaidEncounter("Cataclysm", "Survive the Onslaught"),
          new RaidEncounter("Scission", "Enter the Root"),
          new RaidEncounter("Cataclysm", "Cross the Chasm"),
          new RaidEncounter("Zo’Aurc, Explicator of Planets", "Defeat the Explicator"),
          new RaidEncounter("Nezarec, the Final God of Pain", "Defeat Nezarec")
      ),
      "Root of Nightmares"),
  CROTAS_END(
      Flux.just(
          new RaidEncounter("The Stills", "Traverse the Abyss"),
          new RaidEncounter("The Bridge", "Cross the Bridge"),
          new RaidEncounter("Ir Yut, the Deathsinger", "Reach the Summoning Crystal"),
          new RaidEncounter("Crota, Son of Oryx", "Defeat Crota")
      ),
      "Crota's End");

  @Getter
  private final Flux<RaidEncounter> encounters;

  @Getter
  private final String label;

  Raid(Flux<RaidEncounter> encounters, String label) {
    this.encounters = encounters;
    this.label = label;
  }

}
