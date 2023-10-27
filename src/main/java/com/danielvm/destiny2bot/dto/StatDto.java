package com.danielvm.destiny2bot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatDto {

    /**
     * The name of the stat
     */
    private String statName;

    /**
     * The level of the stat
     */
    private Integer statLevel;
}
