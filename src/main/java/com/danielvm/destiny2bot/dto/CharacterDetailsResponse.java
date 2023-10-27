package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterDetailsResponse {

    /**
     * List of characters for a user
     */
    private List<CharacterDetailDto> characters;
}
