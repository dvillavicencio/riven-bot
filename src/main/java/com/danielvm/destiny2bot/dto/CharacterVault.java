package com.danielvm.destiny2bot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharacterVault {

    /**
     * The list of weapons in the vault of the user
     */
    @NotNull
    private List<CharacterWeapon> weapons;

    /**
     * The size of the vault of the user
     */
    @Min(0)
    private Integer vaultSize;

}
