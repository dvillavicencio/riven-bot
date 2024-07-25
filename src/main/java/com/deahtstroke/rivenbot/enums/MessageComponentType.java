package com.deahtstroke.rivenbot.enums;

import lombok.Getter;

public enum MessageComponentType {
  ACTION_ROW(1),
  BUTTON(2),
  STRING_SELECT(3),
  TEXT_INPUT(4),
  USER_SELECT(5),
  ROLE_SELECT(6),
  MENTIONABLE_SELECT(7),
  CHANNEL_SELECT(8);

  @Getter
  private final Integer type;

  MessageComponentType(Integer type) {
    this.type = type;
  }

}
