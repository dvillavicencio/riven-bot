package com.danielvm.destiny2bot.dto.destiny.characters;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharacterWeaponsResponse {

    /**
     * List of weapons per inventory for all characters
     */
    List<? extends Inventory> inventories;
}
