package com.danielvm.destiny2bot.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

public enum ItemSubTypeEnum {

    AUTO_RIFLE(6),
    SHOTGUN(7),
    MACHINE_GUN(8),
    HAND_CANNON(9),
    ROCKET_LAUNCHER(10),
    FUSION_RIFLE(11),
    SNIPER_RIFLE(12),
    PULSE_RIFLE(13),
    SCOUT_RIFLE(14),
    SIDE_ARM(17),
    SWORD(18),
    LINEAR_FUSION_RIFLE(22),
    GRENADE_LAUNCHER(23),
    SUB_MACHINE_GUN(24),
    TRACE_RIFLE(25),
    BOW(31),
    GLAIVE(33);

    @Getter
    private final Integer id;

    ItemSubTypeEnum(Integer id) {
        this.id = id;
    }

    public static ItemSubTypeEnum findById(Integer id) {
        return Arrays.stream(ItemSubTypeEnum.values()).filter(i -> Objects.equals(i.id, id))
                .findFirst().orElse(null);
    }
}
