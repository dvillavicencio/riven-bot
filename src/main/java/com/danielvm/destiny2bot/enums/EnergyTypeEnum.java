package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum EnergyTypeEnum {

    ANY(0),
    ARC(1),
    SOLAR(2),
    VOID(3),
    GHOST(4),
    SUBCLASS(5),
    STASIS(6);

    @Getter
    private final Integer energyCode;

    EnergyTypeEnum(Integer energyCode) {
        this.energyCode = energyCode;
    }
}
