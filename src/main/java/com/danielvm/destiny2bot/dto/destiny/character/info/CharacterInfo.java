package com.danielvm.destiny2bot.dto.destiny.character.info;

import lombok.Data;

import java.util.Map;

@Data
public class CharacterInfo {
    private String membershipId;
    private Integer membershipType;
    private String characterId;
    private Integer light;
    private Map<String, Integer> stats;
    private Long classHash;
    private Long classType;
}
