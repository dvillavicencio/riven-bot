package com.deahtstroke.rivenbot.enums;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;

public enum InteractionType {
  PING(1),
  APPLICATION_COMMAND(2),
  MESSAGE_COMPONENT(3),
  APPLICATION_COMMAND_AUTOCOMPLETE(4),
  MODAL_SUBMIT(5);

  @Getter
  private final Integer type;

  InteractionType(Integer type) {
    this.type = type;
  }

  /**
   * Find an InteractionType type by their integer code
   *
   * @param type The integer code of the interaction
   * @return {@link InteractionType}
   */
  public static InteractionType findByValue(Integer type) {
    return Arrays.stream(InteractionType.values())
        .filter(it -> Objects.equals(it.type, type))
        .findFirst().orElse(null);
  }
}
