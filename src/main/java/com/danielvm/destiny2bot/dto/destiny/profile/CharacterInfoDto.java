package com.danielvm.destiny2bot.dto.destiny.profile;

import lombok.Data;

import java.util.Map;

@Data
public class CharacterInfoDto {

    /**
     * The Destiny 2 membershipId
     */
    private String membershipId;

    /**
     * The platform this character currently work on
     */
    private Integer membershipType;

    /**
     * The unique character Id
     */
    private String characterId;

    /**
     * Light level for this character
     */
    private Integer light;

    /**
     * Map of stat definition hashes and stat values
     */
    private Map<String, Integer> stats;

    /**
     * The class definition hash
     */
    private Long classHash;

    /**
     * The class type (This is an Enum instead of hash)
     */
    private Long classType;

}