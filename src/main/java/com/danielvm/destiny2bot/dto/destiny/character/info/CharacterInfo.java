package com.danielvm.destiny2bot.dto.destiny.character.info;

import java.util.Map;

public record CharacterInfo(
        String membershipId,
        Integer membershipType,
        String characterId,
        Integer light,
        Map<String, Integer> stats,
        Long classHash,
        Long classType) {
}
