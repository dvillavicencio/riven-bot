package com.danielvm.destiny2bot.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AmmunitionTypeEnum {

    PRIMARY("Primary", 1),
    SPECIAL("Special", 2),
    HEAVY("Heavy", 3);

    @Getter
    private final String label;

    @Getter
    private final Integer code;

    AmmunitionTypeEnum(String label, Integer code) {
        this.label = label;
        this.code = code;
    }

    public static class Mapped {
        public static Map<Integer, AmmunitionTypeEnum> mapped = Arrays.stream(AmmunitionTypeEnum.values())
                .collect(Collectors.toMap(AmmunitionTypeEnum::getCode, a -> a));
    }

    /**
     * Retrieves ammunition type based on code
     *
     * @param code The ammunition code from Bungie
     * @return {@link AmmunitionTypeEnum}
     */
    public static AmmunitionTypeEnum findByCode(Integer code) {
        return Mapped.mapped.get(code);
    }
}
