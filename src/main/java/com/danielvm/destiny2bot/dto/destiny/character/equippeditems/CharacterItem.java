package com.danielvm.destiny2bot.dto.destiny.character.equippeditems;

import lombok.Data;

@Data
public class CharacterItem {
    private Integer itemHash;
    private String itemInstanceId;
    private Integer quantity;
    private Integer bucketHash;

}
