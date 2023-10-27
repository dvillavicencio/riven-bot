package com.danielvm.destiny2bot.dto.destiny.characters;

import lombok.Data;

import java.util.List;

@Data
public class WeaponDetails {

    /**
     * Name of the weapon
     */
    private String weaponName;

    /**
     * Type of the weapon: arc, strand, solar, void, stasis, kinetic
     */
    private String weaponType;

    /**
     * The light level of the weapon
     */
    private Integer weaponLevel;

    private List<?> stats;
 }
