package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum ActivityModeEnum {

  STORY("story"),
  STRIKE("strike"),
  DUNGEON("dungeon"),
  RAID("raid");

  @Getter
  private final String label;

  ActivityModeEnum(String label) {
    this.label = label;
  }
}
