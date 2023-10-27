package com.danielvm.destiny2bot.dto.destiny.characters;

import java.util.List;

public interface Inventory {

    /**
     * Get the name of the inventory
     *
     * @return the name of the inventory
     */
    String getInventoryName();

    /**
     * Get all the weapons for the given inventory
     *
     * @return List of {@link WeaponDetails}
     */
    List<WeaponDetails> getWeapons();
}
