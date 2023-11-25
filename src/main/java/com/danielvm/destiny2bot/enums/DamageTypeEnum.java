package com.danielvm.destiny2bot.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum DamageTypeEnum {

  PRIMARY("Primary", 1),
  SPECIAL("Special", 2),
  HEAVY("Heavy", 3);

  @Getter
  private final String label;

  @Getter
  private final Integer code;

  DamageTypeEnum(String label, Integer code) {
    this.label = label;
    this.code = code;
  }

  public static class Mapped {

    public static Map<Integer, DamageTypeEnum> mapped = Arrays.stream(DamageTypeEnum.values())
        .collect(Collectors.toMap(DamageTypeEnum::getCode, a -> a));
  }

  /**
   * Retrieves ammunition type based on code
   *
   * @param code The ammunition code from Bungie
   * @return {@link DamageTypeEnum}
   */
  public static DamageTypeEnum findByCode(Integer code) {
    return Mapped.mapped.get(code);
  }
}
