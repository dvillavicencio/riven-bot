package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterDetailDto {

    /**
     * The class of this character
     */
    private String className;

    /**
     * List of stats related to this character
     */
    private List<StatDto> stats;
}
