package com.deahtstroke.rivenbot.enums;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;

public enum MessageComponentId {
  RAID_STATS_COMPREHENSION("raid_stats_comprehension"),
  MESSAGE_COMPONENT_TEST("message_component_test"),
  RIVEN_INVITE("riven_invite");

  @Getter
  private final String id;

  MessageComponentId(String componentId) {
    this.id = componentId;
  }

  public static MessageComponentId findById(String id) {
    return Stream.of(MessageComponentId.values())
        .filter(mci -> Objects.equals(mci.id, id))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(
            "Was not able to find a message component ID for id [%s]".formatted(id)));
  }
}
