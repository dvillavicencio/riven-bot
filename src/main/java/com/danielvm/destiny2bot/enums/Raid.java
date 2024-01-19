package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum Raid {
  LAST_WISH("last_wish"),
  GARDEN_OF_SALVATION("garden_of_salvation"),
  DEEP_STONE_CRYPT("deep_stone_crypt"),
  VAULT_OF_GLASS("vault_of_glass"),
  VOW_OF_THE_DISCIPLE("vow_of_the_disciple"),
  KINGS_FALL("kings_fall"),
  ROOT_OF_NIGHTMARES("root_of_nightmares"),
  CROTAS_END("crotas_end");

  @Getter
  private final String label;

  Raid(String label) {
    this.label = label;
  }

}
