package com.danielvm.destiny2bot.dto.destiny.character.info;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Data
public class Characters {

    @Nonnull
    private CharacterData characters;
}
