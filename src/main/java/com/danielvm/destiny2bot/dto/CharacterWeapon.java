package com.danielvm.destiny2bot.dto;

import com.danielvm.destiny2bot.enums.ItemSubTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CharacterWeapon {

    /**
     * Id of the item
     */
    private String itemInstanceId;

    /**
     * Icon of the item
     */
    private String weaponIcon;

    /**
     * Name of the item, most likely a weapon
     */
    private String weaponName;

    /**
     * Where this item currently resides, either a character or in a vault
     */
    private String bucketName;

    /**
     * Weapon type,
     */
    private ItemSubTypeEnum weaponType;

    /**
     * Damage type of the weapon
     */
    private String damageType;

    /**
     * Stats of the weapon
     */
    private List<Stats> stats;

    /**
     * Perks of the weapon
     */
    private List<Perk> perks;
}
