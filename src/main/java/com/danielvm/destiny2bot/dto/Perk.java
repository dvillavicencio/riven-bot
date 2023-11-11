package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Perk {

    /**
     * The name of the weapon perk
     */
    private String perkName;

    /**
     * The description of the perk
     */
    private String perkDescription;

    /**
     * The icon of the perk
     */
    private String perkIcon;
}
