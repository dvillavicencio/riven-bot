package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum EntityTypeEnum {

    BUCKET_DEFINITION("DestinyInventoryItemDefinition"),
    STAT_DEFINITION("DestinyStatDefinition"),
    CLASS_DEFINITION("DestinyClassDefinition");

    @Getter
    private final String identifier;

    EntityTypeEnum(String identifier) {
        this.identifier = identifier;
    }
}
