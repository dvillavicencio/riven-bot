package com.danielvm.destiny2bot.dto.destiny.characters;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VaultInventory implements Inventory {

    /**
     * Name of the inventory
     */
    private String inventoryName;

    /**
     * List of weapons of the inventory
     */
    private List<WeaponDetails> weapons;
}
