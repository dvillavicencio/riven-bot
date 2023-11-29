package com.danielvm.destiny2bot.enums;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;

/**
 * Refer back to
 * <a
 * href="https://bungie-net.github.io/multi/schema_Destiny-DestinyItemType.html#schema_Destiny-DestinyItemType">
 * ItemType Enum</a>in Bungie's API
 */
public enum ItemTypeEnum {

  ARMOR(2),
  WEAPON(3),
  MESSAGE(7);

  @Getter
  private final Integer code;

  ItemTypeEnum(Integer code) {
    this.code = code;
  }

  public static ItemTypeEnum findByCode(Integer code) {
    var enumValue = Arrays.stream(ItemTypeEnum.values())
        .filter(ite -> Objects.equals(ite.code, code))
        .findFirst();
    return enumValue.orElse(null);
  }
}
