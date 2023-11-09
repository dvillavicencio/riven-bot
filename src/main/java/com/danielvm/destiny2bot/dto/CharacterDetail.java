package com.danielvm.destiny2bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterDetail {

    /**
     * The class of the character (e.g.: Titan, Warlock, Hunter)
     */
    private String className;

    /**
     * List of stats for a character (e.g.: Strength, Mobility, Discipline, etc...)
     */
    private List<Stats> stats;
}
