package com.danielvm.destiny2bot.dto.destiny.characters;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharacterInventory implements Inventory {

    /**
     * The name of the inventory
     */
    private String inventoryName;

    /**
     * The character subclass
     */
    private String characterSubclass;

    /**
     * The light level of the character
     */
    private String characterLightLevel;

    /**
     * List of weapons per inventory
     */
    private List<WeaponDetails> weapons;
}
