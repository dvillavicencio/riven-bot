package com.danielvm.destiny2bot.dto.destiny.manifest;

import lombok.Data;

import java.util.Map;

@Data
public class Stats {
    
    private Long statGroupHash;
    
    private Map<String, StatDetails> stats;

    private Long primaryBaseStatHash;
}
