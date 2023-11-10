package com.danielvm.destiny2bot.dto.destiny.manifest;

import lombok.Data;

@Data
public class Stats {
    
    private Long statGroupHash;
    
    private StatValues stats;

    private Long primaryBaseStatHash;
}
