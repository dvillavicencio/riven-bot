package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CharacterWeaponsResponse {

    /**
     * Bucket name where the items currently reside
     */
    private String bucketName;

    /**
     * The list of the weapons in this bucket
     */
    private List<CharacterWeapon> weapons;

}
